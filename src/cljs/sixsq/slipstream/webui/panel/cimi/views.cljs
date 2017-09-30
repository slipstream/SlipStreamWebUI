(ns sixsq.slipstream.webui.panel.cimi.views
  (:require
    [re-com.core :refer [h-box v-box box gap input-text button label modal-panel throbber
                         single-dropdown hyperlink scroller selection-list title]]
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

(defn aggregations-table
  []
  (let [aggregations (subscribe [:webui.cimi/aggregations])
        tr (subscribe [:webui.i18n/tr])]
    (fn []
      (when-let [data @aggregations]
        (let [key-fn (comp name first)
              value-fn (comp :value second)]
          [v-box
           :children [[title
                       :level :level3
                       :label (@tr [:aggregation])]
                      [h-box
                       :gap "1ex"
                       :children [[column
                                   :model data
                                   :key-fn key-fn
                                   :value-fn key-fn
                                   :value-class "webui-row-header"]
                                  [column
                                   :model data
                                   :key-fn key-fn
                                   :value-fn value-fn]]]]])))))

(defn search-vertical-result-table []
  (let [search-results (subscribe [:search-listing])
        aggregations (subscribe [:webui.cimi/aggregations])
        collection-name (subscribe [:search-collection-name])
        selected-fields (subscribe [:search-selected-fields])
        cep (subscribe [:webui.main/cloud-entry-point])]
    (fn []
      (let [{:keys [collection-key]} @cep
            resource-collection-key (get collection-key @collection-name)
            results @search-results]
        [scroller
         :scroll :auto
         :child (if (instance? js/Error results)
                  [box :child [:pre (with-out-str (pprint (ex-data results)))]]
                  (let [entries (get results resource-collection-key [])]
                    [v-box
                     :gap "2ex"
                     :children [[vertical-data-table @selected-fields entries]
                                [aggregations-table]]]))]))))

(defn search-header []
  (let [tr (subscribe [:webui.i18n/tr])
        query-params (subscribe [:search-params])
        first-value (reagent/atom "1")
        last-value (reagent/atom "20")
        filter-value (reagent/atom "")
        orderby-value (reagent/atom "")
        select-value (reagent/atom "")
        aggregation-value (reagent/atom "")]
    (fn []
      ;; reset visible values of parameters
      (let [{:keys [$first $last $filter $select $aggregation $orderby]} @query-params]
        (reset! first-value (str (or $first "")))
        (reset! last-value (str (or $last "")))
        (reset! filter-value (str (or $filter "")))
        (reset! orderby-value (str (or $orderby "")))
        (reset! select-value (str (or $select "")))
        (reset! aggregation-value (str (or $aggregation ""))))
      [v-box
       :gap "1ex"
       :children [[h-box
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
                               :class "btn btn-primary"
                               :label (@tr [:search])
                               :on-click #(dispatch [:search])]]]
                  [h-box
                   :gap "3px"
                   :children [[input-text
                               :model orderby-value
                               :placeholder (@tr [:order])
                               :width "200px"
                               :change-on-blur? true
                               :on-change (fn [v]
                                            (reset! orderby-value v)
                                            (dispatch [:evt.webui.cimi/set-orderby v]))]
                              [input-text
                               :model select-value
                               :placeholder (@tr [:select])
                               :width "200px"
                               :change-on-blur? true
                               :on-change (fn [v]
                                            (reset! select-value v)
                                            (dispatch [:evt.webui.cimi/set-select v]))]
                              [input-text
                               :model aggregation-value
                               :placeholder (@tr [:aggregation])
                               :width "200px"
                               :change-on-blur? true
                               :on-change (fn [v]
                                            (reset! aggregation-value v)
                                            (dispatch [:evt.webui.cimi/set-aggregation v]))]]]]])))

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
                             :gap "1ex"
                             :children [[selection-list
                                         :model selections
                                         :choices available-fields
                                         :multi-select? true
                                         :disabled? false
                                         :height "200px"
                                         :on-change #(reset! selections %)]
                                        [h-box
                                         :justify :between
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
        cep (subscribe [:webui.main/cloud-entry-point])
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
      (let [{:keys [completed? collection-name] {:keys [resources]} :cache} @search]
        (if (instance? js/Error resources)
          [h-box
           :children [[label :label "ERROR"]]]
          [h-box
           :children [#_[box
                         :justify :center
                         :align :center
                         :width "30px"
                         :height "30px"
                         :child (if completed? "" [throbber :size :small])]
                      (if resources
                        (let [total (:count resources)
                              n (count (get resources (keyword collection-name) []))]
                          [title
                           :level :level3
                           :label (str "Results: " n " / " total)])
                        [title
                         :level :level3
                         :label "Results: ?? / ??"])
                      [gap :size "1"]
                      #_(if-let [ops (:operations resources)]
                        (format-operations ops))]])))))

(defn cimi-resource
  []
  (let [cep (subscribe [:webui.main/cloud-entry-point])
        path (subscribe [:webui.main/nav-path])
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
