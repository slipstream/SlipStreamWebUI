(ns sixsq.slipstream.webui.panel.credential.views-table
  (:require
    [re-com.core :refer [h-box v-box input-text button modal-panel scroller selection-list]]
    [sixsq.slipstream.webui.components.core :refer [column]]

    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]

    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]

    [sixsq.slipstream.webui.panel.credential.events]
    [sixsq.slipstream.webui.panel.credential.subs]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.resource :as resource]))

(defn id-selector-formatter
  [entry]
  (let [id (:id entry)
        short-id (or (second (str/split id #"[^A-Za-z0-9]+")) id)]
    short-id))

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
                                          :on-remove #(dispatch [:evt.webui.credential/remove-selected-field selected-field])
                                          :header selected-field
                                          :class "webui-column"
                                          :header-class "webui-column-header"
                                          :value-class "webui-column-value"]))]])

(defn search-vertical-result-table []
  (let [search-results (subscribe [:webui.credential/resources])
        collection-name (subscribe [:webui.credential/collection-name])
        selected-fields (subscribe [:webui.credential/selected-fields])
        cep (subscribe [:webui.main/cloud-entry-point])]
    (fn []
      (let [{:keys [collection-key]} @cep
            resource-collection-key (get collection-key @collection-name)
            results @search-results]
        [scroller
         :scroll :auto
         :child (if (instance? js/Error results)
                  [:pre (with-out-str (pprint (ex-data results)))]
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
                                (dispatch [:evt.webui.credential/set-first v]))]
                  [input-text
                   :model last-value
                   :placeholder (@tr [:last])
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! last-value v)
                                (dispatch [:evt.webui.credential/set-last v]))]
                  [input-text
                   :model filter-value
                   :placeholder (@tr [:filter])
                   :width "300px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! filter-value v)
                                (dispatch [:evt.webui.credential/set-filter v]))]
                  [button
                   :label (@tr [:search])
                   :on-click #(dispatch [:evt.webui.credential/search])]]])))

(defn select-fields []
  (let [tr (subscribe [:webui.i18n/tr])
        available-fields (subscribe [:webui.credential/available-fields])
        selected-fields (subscribe [:webui.credential/selected-fields])
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
                                          (dispatch [:evt.webui.credential/set-selected-fields @selections]))
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
                                                                 (dispatch [:evt.webui.credential/set-selected-fields @selections]))]]]]]])]])))

(defn select-controls []
  [h-box
   :gap "3px"
   :children [[select-fields]]])

(defn control-bar []
  [h-box
   :justify :between
   :children [[select-controls]
              [search-header]]])

(defn credential-resource
  []
  [v-box
   :gap "1ex"
   :children [[control-bar]
              [search-vertical-result-table]]])
