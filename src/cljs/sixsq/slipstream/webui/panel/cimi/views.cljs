(ns sixsq.slipstream.webui.panel.cimi.views
  (:require
    [re-com.core :refer [h-box v-box box gap input-text title
                         button row-button label modal-panel throbber
                         single-dropdown hyperlink
                         scroller selection-list]]
    [reagent.core :as reagent]
    [clojure.pprint :refer [pprint]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.panel.cimi.effects]
    [sixsq.slipstream.webui.panel.cimi.events]
    [sixsq.slipstream.webui.panel.cimi.subs]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.resource :as resource]
    [sixsq.slipstream.webui.widget.breadcrumbs.views :as breadcrumbs]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.doc-render-utils :as doc-utils]
    [sixsq.slipstream.webui.widget.operations.views :as ops]))

(defn format-link [[k {:keys [href]}]]
  (let [n (name k)]
    {:id href :label n}))

(defn link? [m]
  (boolean (and (map? m) (:href m))))

(defn cep-opts [cep]
  (let [links (sort (remove (fn [[_ v]] (not (link? v))) cep))]
    (map format-link links)))

(defn href->collection-name
  [cep resource-type]
  (when (and cep resource-type)
    (log/error "MAPPING CEP:" cep resource-type)
    (let [mappings (->> cep
                        (map (juxt #(:href (second %)) #(name (first %)))))]
      (log/error "MAPPING: " mappings)
      (get (into {} mappings) resource-type))))

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

(defn data-field [selected-field entry]
  (fn []
    (let [v (or (get-in entry (utils/id->path selected-field)) "\u00a0")
          align (if (re-matches #"[0-9\.-]+" (str v)) :end :start)]
      (if (= "id" selected-field)
        [box :align align :child [hyperlink :label v :on-click (fn []
                                                                 (dispatch [:set-resource-data entry])
                                                                 (history/navigate (str "cimi/" v)))]]
        [box :align align :child [label :label v]]))))

(defn column-header-with-key [selected-field]
  ^{:key (str "column-header-" selected-field)}
  [h-box
   :justify :between
   :gap "1ex"
   :align :center
   :class "webui-column-header"
   :children [[label
               :label selected-field]
              (if-not (= "id" selected-field)
                [row-button
                 :md-icon-name "zmdi zmdi-close"
                 :mouse-over-row? true
                 :tooltip "remove column"
                 :on-click #(dispatch [:remove-selected-field selected-field])])]])

(defn data-field-with-key [selected-field entry]
  (let [k (str "data-" selected-field "-" (:id entry))]
    ^{:key k} [data-field selected-field entry]))

(defn data-column-with-key [entries selected-field]
  ^{:key (str "column-" selected-field)}
  [v-box
   :class "webui-column"
   :children [(column-header-with-key selected-field)
              (doall (map (partial data-field-with-key selected-field) entries))]])

(defn vertical-data-table [selected-fields entries]
  [h-box
   :class "webui-column-table"
   :children [(doall (map (partial data-column-with-key entries) selected-fields))]])

(defn search-vertical-result-table []
  (let [search-results (subscribe [:search-listing])
        collection-name (subscribe [:search-collection-name])
        selected-fields (subscribe [:search-selected-fields])
        cep (subscribe [:cloud-entry-point])]
    (fn []
      (let [{:keys [collection-key]} @cep
            resource-collection-key (get collection-key @collection-name)
            results @search-results]
        [scroller
         :scroll :auto
         :child (if (instance? js/Error results)
                  [box :child (format-data (ex-data results))]
                  (let [entries (get results resource-collection-key [])]
                    [vertical-data-table @selected-fields entries]))]))))

(defn search-header []
  (let [tr (subscribe [:webui.i18n/tr])
        first-value (reagent/atom "1")
        last-value (reagent/atom "20")
        filter-value (reagent/atom "")]
    (fn []
      [h-box
       :gap "3px"
       :children [[input-text
                   :model first-value
                   :placeholder (@tr [:first])
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! first-value v)
                                (dispatch [:set-search-first v]))]
                  [input-text
                   :model last-value
                   :placeholder (@tr [:last])
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! last-value v)
                                (dispatch [:set-search-last v]))]
                  [input-text
                   :model filter-value
                   :placeholder (@tr [:filter])
                   :width "300px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! filter-value v)
                                (dispatch [:set-search-filter v]))]
                  [button
                   :label (@tr [:search])
                   :on-click #(dispatch [:search])]]])))

(defn select-fields []
  (let [tr (subscribe [:webui.i18n/tr])
        available-fields (subscribe [:search-available-fields])
        selected-fields (subscribe [:search-selected-fields])
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
                                          (dispatch [:set-selected-fields @selections]))
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
                                         :children [[button
                                                     :label (@tr [:cancel])
                                                     :on-click (fn []
                                                                 (reset! show? false))]
                                                    [button
                                                     :label (@tr [:update])
                                                     :class "btn-primary"
                                                     :on-click (fn []
                                                                 (reset! show? false)
                                                                 (dispatch [:set-selected-fields @selections]))]]]]]])]])))

(defn cloud-entry-point
  []
  (let [tr (subscribe [:webui.i18n/tr])
        cep (subscribe [:cloud-entry-point])
        selected-id (subscribe [:search-collection-name])]
    (fn []
      [h-box
       :children [[single-dropdown
                   :model selected-id
                   :placeholder (@tr [:resource-type])
                   :width "250px"
                   :choices (vec (map (fn [k] {:id k :label k}) (sort (vals (:collection-href @cep)))))
                   :on-change (fn [id]
                                (history/navigate (str "cimi/" (name id))))]]])))

(defn select-controls []
  [h-box
   :gap "3px"
   :children [[cloud-entry-point]
              [select-fields]]])

(defn control-bar []
  [h-box
   :justify :between
   :children [[select-controls]
              [search-header]]])

(defn action-bar []
  (let [cep (subscribe [:cloud-entry-point])
        search (subscribe [:search])]
    (fn []
      (let [{:keys [baseURI]} @cep
            {{:keys [operations]} :listing :as data} @search]
        (when operations
          (ops/format-operations (:listing data) baseURI))))))

(defn results-bar []
  (let [search (subscribe [:search])]
    (fn []
      (let [{:keys [completed? results collection-name]} @search]
        (if (instance? js/Error results)
          [h-box
           :children [[label :label "ERROR"]]]
          [h-box
           :children [[box
                       :justify :center
                       :align :center
                       :width "30px"
                       :height "30px"
                       :child (if completed? "" [throbber :size :small])]
                      (if results
                        (let [total (:count results)
                              n (count (get results (keyword collection-name) []))]
                          [label
                           :label (str "Results: " n " / " total)])
                        [label :label "Results: ?? / ??"])
                      [gap :size "1"]
                      (if-let [ops (:operations results)]
                        (format-operations ops))]])))))

(defn cimi-resource
  []
  (let [cep (subscribe [:cloud-entry-point])
        path (subscribe [:resource-path])
        data (subscribe [:resource-data])]
    (fn []
      (let [[_ resource-type resource-id] @path]
        (dispatch [:set-collection-name resource-type]))
      (let [n (count @path)
            children (case n
                       1 [[control-bar]
                          [action-bar]]
                       2 [[control-bar]
                          [action-bar]
                          [results-bar]
                          [search-vertical-result-table]]
                       3 [[doc-utils/resource-detail @data (:baseURI @cep)]]
                       [[control-bar]])]
        [v-box
         :gap "1ex"
         :children children]))))

(defmethod resource/render "cimi"
  [path query-params]
  [cimi-resource])
