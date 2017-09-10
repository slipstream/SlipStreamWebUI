(ns sixsq.slipstream.webui.panel.offer.views
  (:require
    [re-com.core :refer [h-box v-box box input-text
                         button row-button label modal-panel throbber
                         hyperlink scroller selection-list title]]
    [sixsq.slipstream.webui.components.core :refer [column]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.pprint :refer [pprint]]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.panel.offer.effects]
    [sixsq.slipstream.webui.panel.offer.events]
    [sixsq.slipstream.webui.panel.offer.subs]
    [sixsq.slipstream.webui.widget.history.utils :as history]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.resource :as resource]
    [sixsq.slipstream.webui.doc-render-utils :as doc-utils]
    [taoensso.timbre :as log]))

(defn format-id [id]
  (second (re-matches #"[^/]+/(.{8}).*" id)))

(defn offer-uuid [id]
  (second (re-matches #"[^/]+/(.+)" id)))

(defn id-selector-formatter [entry]
  (let [v (:id entry)]
    [box :align :start
     :child [hyperlink
             :label (format-id v)
             :on-click #(history/navigate (str "offer/" (offer-uuid v)))]]))

(defn vertical-data-table [selected-fields entries]
  [h-box
   :class "webui-column-table"
   :children [(doall
                (for [selected-field selected-fields]
                  ^{:key selected-field} [column
                                          :model entries
                                          :key-fn :id
                                          :value-fn (if (= "id" selected-field)
                                                      id-selector-formatter
                                                      (keyword selected-field))
                                          :on-remove #(dispatch [:remove-selected-field selected-field])
                                          :header selected-field
                                          :class "webui-column"
                                          :header-class "webui-column-header"
                                          :value-class "webui-column-value"]))]])

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
                    [box :child [:pre (with-out-str (pprint (ex-data results)))]]
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
      (reset! selections (set @selected-fields))
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
