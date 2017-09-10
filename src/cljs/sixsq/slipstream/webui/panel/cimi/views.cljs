(ns sixsq.slipstream.webui.panel.cimi.views
  (:require
    [re-com.core :refer [h-box v-box box gap input-text button label modal-panel throbber
                         single-dropdown hyperlink scroller selection-list]]
    [sixsq.slipstream.webui.components.core :refer [column]]
    [reagent.core :as reagent]
    [clojure.pprint :refer [pprint]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.panel.cimi.events]
    [sixsq.slipstream.webui.panel.cimi.subs]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.resource :as resource]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.doc-render-utils :as doc-utils]))

(defn format-operations
  [ops]
  [h-box
   :gap "1ex"
   :children
   (doall (map (fn [{:keys [rel href]}] [hyperlink
                                         :label rel
                                         :on-click #(js/alert (str "Operation: " rel " on URL " href))]) ops))])

(defn id-selector-formatter [entry]
  (let [v (:id entry)]
    [box
     :align
     :start
     :child [hyperlink
             :label v
             :on-click (fn []
                         (dispatch [:set-resource-data entry])
                         (history/navigate (str "cimi/" v)))]]))

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
                  [box :child [:pre (with-out-str (pprint (ex-data results)))]]
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
      (reset! selections (set @selected-fields))
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
                       1 [[control-bar]]
                       2 [[control-bar]
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
