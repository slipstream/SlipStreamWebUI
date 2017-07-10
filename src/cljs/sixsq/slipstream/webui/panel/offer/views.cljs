(ns sixsq.slipstream.webui.panel.offer.views
  (:require
    [re-com.core :refer [h-box v-box box input-text
                         button row-button label modal-panel throbber
                         hyperlink scroller selection-list title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.panel.offer.effects]
    [sixsq.slipstream.webui.panel.offer.events]
    [sixsq.slipstream.webui.panel.offer.subs]
    [sixsq.slipstream.webui.widget.history.utils :as history]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.resource :as resource]
    [sixsq.slipstream.webui.widget.breadcrumbs.views :as breadcrumbs]
    [sixsq.slipstream.webui.doc-render-utils :as doc-utils]
    [taoensso.timbre :as log]))

(defn format-operations
  [ops]
  [h-box
   :gap "1ex"
   :children
   (doall (map (fn [{:keys [rel href]}] [hyperlink
                                         :label rel
                                         :on-click #(js/alert (str "Operation: " rel " on URL " href))]) ops))])

(declare format-data)

(defn format-list-entry
  [prefix k v]
  (let [id (:id v)]
    ^{:key (str prefix "-" k)}
    [:li [:strong (or id k)] (format-data prefix v)]))

(defn format-map-entry
  [prefix [k v]]
  ^{:key (str prefix "-" k)}
  [:li [:strong k] " : " (if (= k :operations)
                           (format-operations v)
                           (format-data prefix v))])

(defn as-map [prefix m]
  [:ul (doall (map (partial format-map-entry prefix) m))])

(defn as-vec [prefix v]
  [:ul (doall (map (partial format-list-entry prefix) (range) v))])

(defn format-data
  ([v]
   (format-data (str (random-uuid)) v))
  ([prefix v]
   (cond
     (map? v) (as-map prefix v)
     (vector? v) (as-vec prefix v)
     :else (str v))))

(defn format-id [id]
  (second (re-matches #"[^/]+/(.{8}).*" id)))

(defn offer-uuid [id]
  (second (re-matches #"[^/]+/(.+)" id)))

(defn data-field [selected-field entry]
  (fn []
    (let [v (or (get-in entry (utils/id->path selected-field)) "\u00a0")
          align (if (re-matches #"[0-9\.-]+" (str v)) :end :start)]
      (if (= "id" selected-field)
        [box :align align :child [hyperlink
                                  :label (format-id v)
                                  :on-click #(history/navigate (str "offer/" (offer-uuid v)))]]
        [box :align align :child [label :label v]]))))

(defn column-header-with-key [selected-field]
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      ^{:key (str "column-header-" selected-field)}
      [h-box
       :justify :between
       :gap "1ex"
       :align :center
       :class "webui-column-header"
       :children [[label :label selected-field]
                  (if-not (= "id" selected-field)
                    [row-button
                     :md-icon-name "zmdi zmdi-close"
                     :mouse-over-row? true
                     :tooltip (@tr [:remove-column])
                     :on-click #(dispatch [:evt.webui.offer/remove-selected-field selected-field])])]])))

(defn data-field-with-key [selected-field entry]
  (let [k (str "data-" selected-field "-" (:id entry))]
    ^{:key k} [data-field selected-field entry]))

(defn data-column-with-key [entries selected-field]
  ^{:key (str "column-" selected-field)}
  [v-box
   :class "webui-column"
   :children [[column-header-with-key selected-field]
              (doall (map (partial data-field-with-key selected-field) entries))]])

(defn vertical-data-table [selected-fields entries]
  [h-box
   :class "webui-column-table"
   :children [(doall (map (partial data-column-with-key entries) selected-fields))]])

(defn search-vertical-result-table []
  (let [search-results (subscribe [:offer-listing])
        collection-name (subscribe [:offer-collection-name])
        selected-fields (subscribe [:offer-selected-fields])
        cloud-entry-point (subscribe [:cloud-entry-point])]
    (fn []
      (let [results @search-results
            {:keys [collection-key]} @cloud-entry-point]
        (when (and collection-name collection-key)
          [scroller
           :scroll :auto
           :child (if (instance? js/Error results)
                    [box :child (format-data (ex-data results))]
                    (let [entries (get results (collection-key @collection-name) [])]
                      [vertical-data-table @selected-fields entries]))])))))

(defn search-header []
  (let [tr (subscribe [:webui.i18n/tr])
        first-value (subscribe [:offer-params-first])
        last-value (subscribe [:offer-params-last])
        filter-value (subscribe [:offer-params-filter])
        first-atom (reagent/atom (str @first-value))
        last-atom (reagent/atom (str @last-value))
        filter-atom (reagent/atom (or @filter-value ""))]
    (fn []
      [h-box
       :gap "3px"
       :children [[input-text
                   :model first-atom
                   :placeholder (@tr [:first])
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! first-atom v)
                                (dispatch [:evt.webui.offer/set-param-first v]))]
                  [input-text
                   :model last-atom
                   :placeholder (@tr [:last])
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! last-atom v)
                                (dispatch [:evt.webui.offer/set-param-last v]))]
                  [input-text
                   :model filter-atom
                   :placeholder (@tr [:filter])
                   :width "300px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! filter-atom v)
                                (dispatch [:evt.webui.offer/set-param-filter v]))]
                  [button
                   :label (@tr [:search])
                   :on-click #(dispatch [:offer])]]])))

(defn select-fields []
  (let [tr (subscribe [:webui.i18n/tr])
        available-fields (subscribe [:offer-available-fields])
        selected-fields (subscribe [:offer-selected-fields])
        selections (reagent/atom #{})
        show? (reagent/atom false)]
    (fn []
      (reset! selections @selected-fields)
      [h-box
       :children [[button
                   :label (@tr [:fields])
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click (fn []
                                          (reset! show? false)
                                          (dispatch [:evt.webui.offer/set-selected-fields @selections]))
                     :child [v-box
                             :width "350px"
                             :children [[selection-list
                                         :model selections
                                         :choices available-fields
                                         :multi-select? true
                                         :disabled? false
                                         :height "200px"
                                         :on-change #(reset! selections %)]
                                        [h-box
                                         :justify :end
                                         :gap "3px"
                                         :children [[button
                                                     :label (@tr [:cancel])
                                                     :on-click (fn []
                                                                 (reset! show? false))]
                                                    [button
                                                     :label (@tr [:update])
                                                     :class "btn-primary"
                                                     :on-click (fn []
                                                                 (reset! show? false)
                                                                 (dispatch [:evt.webui.offer/set-selected-fields @selections]))]]]]]])]])))

(defn select-controls []
  [h-box
   :gap "3px"
   :children [[select-fields]]])

(defn control-bar []
  [h-box
   :justify :between
   :children [[select-controls]
              [search-header]]])

(defn results-bar []
  (let [tr (subscribe [:webui.i18n/tr])
        search (subscribe [:offer])]
    (fn []
      (let [{:keys [completed? results collection-name]} @search]
        (if (instance? js/Error results)
          [h-box
           :children [[label :label (@tr [:error])]]]
          [h-box
           :gap "2px"
           :children [[label :label (@tr [:results])]
                      (when results
                        (let [total (:count results)
                              n (count (get results (keyword collection-name) []))]
                          [label :label (str " " n " / " total)]))
                      (when-not completed? [throbber :size :regular])]])))))

(defn offer-panel
  []
  (let [cep (subscribe [:cloud-entry-point])
        path (subscribe [:resource-path])
        data (subscribe [:offer-data])]
    (fn []
      (let [listing? (= 1 (count @path))
            children (if listing?
                       [[control-bar]
                        [results-bar]
                        [search-vertical-result-table]]
                       [[doc-utils/resource-detail @data (:baseURI @cep)]])]
        [v-box
         :gap "1ex"
         :children children]))))

(defmethod resource/render "offer"
  [path query-params]
  (dispatch [:set-offer query-params])
  (when (second path) (dispatch [:set-offer-detail (second path)]))
  [offer-panel])
