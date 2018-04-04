(ns sixsq.slipstream.webui.cimi.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as reagent]

    [clojure.pprint :refer [pprint]]
    [clojure.set :as set]
    [clojure.string :as str]

    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.editor.editor :as editor]
    [sixsq.slipstream.webui.utils.component :as cutil]
    [sixsq.slipstream.webui.utils.forms :as form-utils]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]

    [sixsq.slipstream.webui.cimi.subs :as cimi-subs]
    [sixsq.slipstream.webui.cimi.events :as cimi-events]
    [sixsq.slipstream.webui.cimi-detail.views :as cimi-detail-views]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.response :as response]

    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [cljs.pprint :as pprint]))


(defn id-selector-formatter [entry]
  (let [v (:id entry)
        label (second (str/split v #"/"))
        on-click #(dispatch [::history-events/navigate (str "cimi/" v)])]
    [:a {:style {:cursor "pointer"} :on-click on-click} label]))


(defn field-selector
  [field]
  (let [ks (map keyword (str/split field #"/"))]
    (fn [m]
      (get-in m ks))))


(defn remove-column-fn
  [label]
  (fn []
    (dispatch [::cimi-events/remove-field label])))


(defn table-header-cell
  [label]
  ^{:key label} [ui/TableHeaderCell
                 [:a {:on-click (remove-column-fn label)} [ui/Icon {:name "remove circle"}]]
                 "\u00a0" label])


(defn results-table-header [selected-fields]
  [ui/TableHeader
   (vec (concat [ui/TableRow]
                (mapv table-header-cell selected-fields)))])


(defn results-table-row-fn [selected-fields]
  (apply juxt (map (fn [selected-field] (if (= "id" selected-field)
                                          id-selector-formatter
                                          (field-selector selected-field)))
                   selected-fields)))


(defn results-table-row [row-fn entry]
  (when entry
    (let [data (row-fn entry)]
      (vec (concat [ui/TableRow]
                   (vec (map (fn [v] [ui/TableCell v]) data)))))))


(defn results-table-body [row-fn entries]
  (vec (concat [ui/TableBody]
               (vec (map (partial results-table-row row-fn) entries)))))


(defn results-table [selected-fields entries]
  (when (pos? (count entries))
    (let [row-fn (results-table-row-fn selected-fields)]
      [:div {:class-name "webui-x-autoscroll"}
       [ui/Table
        {:collapsing  true
         :compact     true
         :unstackable true
         :single-line true
         :padded      false}
        (results-table-header selected-fields)
        (results-table-body row-fn entries)]])))


(defn statistic
  [[value label :as data]]
  (when data
    ^{:key label}
    [ui/Statistic {:size "tiny"}
     (if (int? value)
       [ui/StatisticValue (pprint/cl-format nil "~D" value)]
       [ui/StatisticValue (pprint/cl-format nil "~,2F" value)])
     [ui/StatisticLabel label]]))


(defn results-statistic
  []
  (let [tr (subscribe [::i18n-subs/tr])
        collection-name (subscribe [::cimi-subs/collection-name])
        resources (subscribe [::cimi-subs/collection])
        cep (subscribe [::cimi-subs/cloud-entry-point])]
    (fn []
      (let [collection-name @collection-name
            resources @resources]
        (when resources
          (let [collection-key (get (:collection-key @cep) collection-name)
                total (:count resources)
                n (count (get resources collection-key []))]
            [ui/Statistic
             [ui/StatisticValue (str n " / " total)]
             [ui/StatisticLabel (@tr [:results])]]))))))


(def tuple-fn (juxt (comp :value second)
                    (comp name first)))


(defn aggregations-table
  []
  (let [aggregations (subscribe [::cimi-subs/aggregations])]
    (fn []
      (let [stats (->> @aggregations
                       (map tuple-fn)
                       (sort second)
                       (map #(statistic %))
                       vec)]
        (vec (concat [ui/StatisticGroup {:size :mini}] [[results-statistic]] stats))))))


(defn results-display []
  (let [tr (subscribe [::i18n-subs/tr])
        collection (subscribe [::cimi-subs/collection])
        collection-name (subscribe [::cimi-subs/collection-name])
        selected-fields (subscribe [::cimi-subs/selected-fields])
        cep (subscribe [::cimi-subs/cloud-entry-point])]
    (fn []
      (let [{:keys [collection-key]} @cep
            resource-collection-key (get collection-key @collection-name)
            results @collection]
        [cc/collapsible-card (@tr [:results])
         (if (instance? js/Error results)
           [:pre (with-out-str (pprint (ex-data results)))]
           (let [entries (get results resource-collection-key [])]
             [:div
              [aggregations-table]
              [results-table @selected-fields entries]]))]))))


(defn cloud-entry-point-title
  []
  (let [tr (subscribe [::i18n-subs/tr])
        cep (subscribe [::cimi-subs/cloud-entry-point])
        selected-id (subscribe [::cimi-subs/collection-name])]
    (fn []
      (let [options (->> @cep
                         :collection-href
                         vals
                         sort
                         (map (fn [k] {:value k :text k}))
                         vec)
            callback #(dispatch [::history-events/navigate (str "cimi/" %)])]
        [ui/Dropdown
         {:value       @selected-id
          :placeholder (@tr [:resource-type])
          :scrolling   true
          :options     options
          :on-change   (cutil/callback :value callback)}]))))


(defn search-header []
  (let [tr (subscribe [::i18n-subs/tr])
        filter-visible? (subscribe [::cimi-subs/filter-visible?])
        query-params (subscribe [::cimi-subs/query-params])]
    (fn []
      ;; reset visible values of parameters
      (let [{:keys [$first $last $filter $select $aggregation $orderby]} @query-params]
        [ui/Form
         [ui/FormField
          [cloud-entry-point-title]]
         (when @filter-visible?
           [ui/FormGroup {:widths "equal"}
            [ui/FormField
             ; the key below is a workaround react issue with controlled input cursor position,
             ; this will force to re-render defaultValue on change of the value
             ^{:key (str "first:" $first)}
             [ui/Input {:type         "number"
                        :min          0
                        :label        (@tr [:first])
                        :defaultValue $first
                        :on-blur      #(dispatch [::cimi-events/set-first (-> %1 .-target .-value)])}]]

            [ui/FormField
             ^{:key (str "last:" $last)}
             [ui/Input {:type         "number"
                        :min          0
                        :label        (@tr [:last])
                        :defaultValue $last
                        :on-blur      #(dispatch
                                         [::cimi-events/set-last (-> %1 .-target .-value)])}]]

            [ui/FormField
             ^{:key (str "select:" $select)}
             [ui/Input {:type         "text"
                        :label        (@tr [:select])
                        :defaultValue $select
                        :on-blur      #(dispatch
                                         [::cimi-events/set-select (-> %1 .-target .-value)])}]]])

         (when @filter-visible?
           [ui/FormGroup {:widths "equal"}
            [ui/FormField
             ^{:key (str "orderby:" $orderby)}
             [ui/Input {:type         "text"
                        :label        (@tr [:order])
                        :defaultValue $orderby
                        :on-blur      #(dispatch
                                         [::cimi-events/set-orderby (-> %1 .-target .-value)])}]]

            [ui/FormField
             ^{:key (str "aggregation:" $aggregation)}
             [ui/Input {:type         "text"
                        :label        (@tr [:aggregation])
                        :defaultValue $aggregation
                        :on-blur      #(dispatch [::cimi-events/set-aggregation (-> %1 .-target .-value)])}]]])

         (when @filter-visible?
           [ui/FormGroup {:widths "equal"}
            [ui/FormField
             ^{:key (str "filter:" $filter)}
             [ui/Input
              {:type         "text"
               :label        (@tr [:filter])
               :defaultValue $filter
               :on-blur      #(dispatch [::cimi-events/set-filter (-> %1 .-target .-value)])}]]])]))))


(defn format-field-item [selections-atom item]
  [ui/ListItem
   [ui/ListContent
    [ui/ListHeader
     [ui/Checkbox {:default-checked (contains? @selections-atom item)
                   :label           item
                   :on-change       (cutil/callback :checked
                                                    (fn [checked]
                                                      (if checked
                                                        (swap! selections-atom set/union #{item})
                                                        (swap! selections-atom set/difference #{item}))))}]]]])


(defn format-field-list [available-fields-atom selections-atom]
  (let [items (sort @available-fields-atom)]
    (vec (concat [ui/ListSA]
                 (map (partial format-field-item selections-atom) items)))))


(defn select-fields []
  (let [tr (subscribe [::i18n-subs/tr])
        available-fields (subscribe [::cimi-subs/available-fields])
        selected-fields (subscribe [::cimi-subs/selected-fields])
        selected-id (subscribe [::cimi-subs/collection-name])
        selections (reagent/atom (set @selected-fields))
        show? (reagent/atom false)]
    (fn []
      [ui/MenuItem {:name     "select-fields"
                    :disabled (nil? @selected-id)
                    :on-click (fn []
                                (reset! selections (set @selected-fields))
                                (reset! show? true))}
       [ui/Icon {:name "columns"}]
       (@tr [:columns])
       [ui/Modal
        {:closeIcon true
         :open      @show?
         :on-close  #(reset! show? false)}
        [ui/ModalHeader (@tr [:fields])]
        [ui/ModalContent
         {:scrolling true}
         (format-field-list available-fields selections)]
        [ui/ModalActions
         [ui/Button
          {:on-click #(reset! show? false)}
          (@tr [:cancel])]
         [ui/Button
          {:primary  true
           :on-click (fn []
                       (reset! show? false)
                       (dispatch [::cimi-events/set-selected-fields @selections]))}
          (@tr [:update])]]]])))


(defn resource-add-form
  []
  (let [tr (subscribe [::i18n-subs/tr])
        show? (subscribe [::cimi-subs/show-add-modal?])
        collection-name (subscribe [::cimi-subs/collection-name])
        cloud-entry-point (subscribe [::cimi-subs/cloud-entry-point])
        descriptions-vector-atom (subscribe [::cimi-subs/descriptions-vector])
        text (reagent/atom "")]
    (fn []
      (let [resource-key (get (:collection-key @cloud-entry-point) @collection-name)
            tpl-resource-key (cimi-utils/template-resource-key @cloud-entry-point @collection-name)]
        (when (and tpl-resource-key (empty? @descriptions-vector-atom))
          (log/info "retrieving templates for" tpl-resource-key)
          (dispatch [::cimi-events/get-templates tpl-resource-key]))
        (when @show?
          (cond
            (and tpl-resource-key (seq @descriptions-vector-atom))
            [form-utils/form-container-modal
             :show? show?
             :descriptions descriptions-vector-atom
             :on-cancel #(dispatch [::cimi-events/hide-add-modal])
             :on-submit (fn [data]
                          (dispatch [::cimi-events/create-resource
                                     (cimi-api-utils/create-template @collection-name data)])
                          (dispatch [::cimi-events/hide-add-modal]))]

            :else
            (do
              (reset! text (general/edn->json {:key "value"}))
              [ui/Modal
               {:size       "large"
                :scrollable true
                :closeIcon  true
                :open       @show?}
               [ui/ModalContent
                [editor/json-editor text]]
               [ui/ModalActions
                [ui/Button
                 {:on-click (fn []
                              (dispatch [::cimi-events/hide-add-modal]))}
                 (@tr [:cancel])]
                [ui/Button
                 {:primary  true
                  :on-click (fn []
                              (try
                                (let [data (general/json->edn @text)]
                                  (dispatch [::cimi-events/create-resource data]))
                                (catch js/Error e
                                  (dispatch [::messages-events/add
                                             {:header  "invalid JSON document"
                                              :message (str "invalid JSON:\n\n" e)
                                              :type    :error}]))
                                (finally
                                  (dispatch [::cimi-events/hide-add-modal]))))}
                 (@tr [:create])]]])))))))


(defn can-add?
  [ops]
  (->> ops
       (map :rel)
       (filter #(= "add" %))
       not-empty))


(defn search-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::cimi-subs/loading?])
        selected-id (subscribe [::cimi-subs/collection-name])]
    (fn []
      [ui/MenuItem {:name     "search"
                    :disabled (nil? @selected-id)
                    :on-click #(dispatch [::cimi-events/get-results])}
       (if @loading?
         [ui/Icon {:name    "refresh"
                   :loading @loading?}]
         [ui/Icon {:name "search"}])
       (@tr [:search])])))


(defn create-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        search-results (subscribe [::cimi-subs/collection])]
    (fn []
      (when (can-add? (-> @search-results :operations))
        [ui/MenuItem {:name     "add"
                      :on-click #(dispatch [::cimi-events/show-add-modal])}
         [ui/Icon {:name "add"}]
         (@tr [:add])]))))


(defn filter-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        filter-visible? (subscribe [::cimi-subs/filter-visible?])]
    (fn []
      [ui/MenuMenu {:position "right"}
       [ui/MenuItem {:name     "filter"
                     :on-click #(dispatch [::cimi-events/toggle-filter])}
        [ui/IconGroup
         [ui/Icon {:name "filter"}]
         [ui/Icon {:name   (if @filter-visible? "chevron down" "chevron right")
                   :corner true}]]
        (str "\u00a0" (@tr [:filter]))]])))


(defn menu-bar []
  (let [tr (subscribe [::i18n-subs/tr])
        resources (subscribe [::cimi-subs/collection])]
    (fn []
      (when (instance? js/Error @resources)
        (dispatch [::messages-events/add
                   (let [{:keys [status message]} (response/parse-ex-info @resources)]
                     {:header  (cond-> (@tr [:error])
                                       status (str " (" status ")"))
                      :message message
                      :type    :error})]))
      [:div
       [resource-add-form]
       [ui/Menu {:attached   "top"
                 :borderless true}
        [search-button]
        [select-fields]
        (when (can-add? (-> @resources :operations))
          [create-button])
        [filter-button]]
       [ui/Segment {:attached "bottom"}
        [search-header]]])))


(defn cimi-resource
  []
  (let [path (subscribe [::main-subs/nav-path])
        query-params (subscribe [::main-subs/nav-query-params])]
    (fn []
      (let [[_ resource-type resource-id] @path]
        (dispatch [::cimi-events/set-collection-name resource-type])
        (when @query-params
          (dispatch [::cimi-events/set-query-params @query-params])))
      (let [n (count @path)
            children (case n
                       1 [[menu-bar]]
                       2 [[menu-bar]
                          [results-display]]
                       3 [[cimi-detail-views/cimi-detail]]
                       [[menu-bar]])]
        (vec (concat [:div] children))))))


(defmethod panel/render :cimi
  [path]
  [cimi-resource])
