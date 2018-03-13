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
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.utils.general :as general]

    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]))


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


(defn results-table-header [selected-fields]
  [ui/TableHeader
   (vec (concat [ui/TableRow]
                (vec (map (fn [label] ^{:key label} [ui/TableHeaderCell label]) selected-fields))))])


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
     [ui/StatisticValue value]
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


(defn search-header []
  (let [tr (subscribe [::i18n-subs/tr])
        query-params (subscribe [::cimi-subs/query-params])
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
      [ui/Form
       [ui/FormGroup {:widths "equal"
                      ;:fluid  true
                      }
        [ui/FormField
         [ui/Input {:type          "number"
                    :min           0
                    :label         (@tr [:first])
                    :default-value @first-value
                    :on-change     (cutil/callback :value
                                                   (fn [v]
                                                     (reset! first-value v)
                                                     (dispatch [::cimi-events/set-first v])))}]]

        [ui/FormField
         [ui/Input {:type          "number"
                    :min           0
                    :label         (@tr [:last])
                    :default-value @last-value
                    :on-change     (cutil/callback :value
                                                   (fn [v]
                                                     (reset! last-value v)
                                                     (dispatch [::cimi-events/set-last v])))}]]

        [ui/FormField
         [ui/Input {:type          "text"
                    :label         (@tr [:select])
                    :default-value @select-value
                    :on-change     (cutil/callback :value
                                                   (fn [v]
                                                     (reset! select-value v)
                                                     (dispatch [::cimi-events/set-select v])))}]]]

       [ui/FormGroup {:widths "equal"}
        [ui/FormField
         [ui/Input {:type          "text"
                    :label         (@tr [:order])
                    :default-value @orderby-value
                    :on-change     (cutil/callback :value
                                                   (fn [v]
                                                     (reset! orderby-value v)
                                                     (dispatch [::cimi-events/set-orderby v])))}]]


        [ui/FormField
         [ui/Input {:type          "text"
                    :label         (@tr [:aggregation])
                    :default-value @aggregation-value
                    :on-change     (cutil/callback :value
                                                   (fn [v]
                                                     (reset! aggregation-value v)
                                                     (dispatch [::cimi-events/set-aggregation v])))}]]]

       [ui/FormGroup {;:fluid  true
                      :widths "equal"}
        [ui/FormField
         [ui/Input {:type          "text"
                    :label         (@tr [:filter])
                    :default-value @filter-value
                    :on-change     (cutil/callback :value
                                                   (fn [v]
                                                     (reset! filter-value v)
                                                     (dispatch [::cimi-events/set-filter v])))}]]]])))


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
        selections (reagent/atom (set @selected-fields))
        show? (reagent/atom false)]
    (fn []
      [ui/MenuItem {:name     "select-fields"
                    :on-click #(reset! show? true)}
       [ui/Icon {:name "columns"}]
       (@tr [:columns])
       [ui/Modal
        {:closeIcon true
         :open      @show?
         :on-close  #(reset! show? false)
         :on-open   #(reset! selections (set @selected-fields))}
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
         {:as          :h1
          :value       @selected-id
          :placeholder (@tr [:resource-type])
          :inline      true
          :options     options
          :on-change   (cutil/callback :value callback)}]))))


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
                                  (dispatch [::main-events/set-message {:header  "Error"
                                                                        :message (str "Unable to parse your json. " e)
                                                                        :error   true}]))
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
        loading? (subscribe [::cimi-subs/loading?])]
    (fn []
      [ui/MenuItem {:name     "search"
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


(defn results-bar []
  (let [tr (subscribe [::i18n-subs/tr])
        resources (subscribe [::cimi-subs/collection])]
    (fn []
      (when (instance? js/Error @resources)
        (dispatch [::main-events/set-message {:header  (@tr [:error])
                                              :message (str @resources)
                                              :error   true}]))
      [ui/Menu
       [search-button]
       [select-fields]
       (when (can-add? (-> @resources :operations))
         [create-button])])))


(defn control-bar
  []
  [cc/collapsible-card
   [cloud-entry-point-title]
   [resource-add-form]
   [search-header]])


(defn cimi-resource
  []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (let [[_ resource-type resource-id] @path]
        (dispatch [::cimi-events/set-collection-name resource-type]))
      (let [n (count @path)
            children (case n
                       1 [[results-bar]
                          [control-bar]]
                       2 [[results-bar]
                          [control-bar]
                          [results-display]]
                       3 [[cimi-detail-views/cimi-detail]]
                       [[results-bar]
                        [control-bar]])]
        (vec (concat [:div] children))))))


(defmethod panel/render :cimi
  [path]
  [cimi-resource])
