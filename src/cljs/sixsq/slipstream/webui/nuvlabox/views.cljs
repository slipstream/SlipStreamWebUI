(ns sixsq.slipstream.webui.nuvlabox.views
  (:require
    [cljs.pprint :refer [cl-format pprint]]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.nuvlabox-detail.events :as nuvlabox-detail-events]
    [sixsq.slipstream.webui.nuvlabox-detail.views :as nuvlabox-detail]
    [sixsq.slipstream.webui.nuvlabox.events :as nuvlabox-events]
    [sixsq.slipstream.webui.nuvlabox.subs :as nuvlabox-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.response :as response]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.component :as cutil]))


(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::nuvlabox-subs/loading?])]
    (fn []
      [ui/MenuItem {:name     "refresh"
                    :on-click #(dispatch [::nuvlabox-events/fetch-state-info])}
       [ui/Icon {:name    "refresh"
                 :loading @loading?}]
       (@tr [:refresh])])))


(defn id-as-link
  [id]
  (let [mac (-> id (str/split #"/") second)
        on-click #(dispatch [::history-events/navigate (str "nuvlabox/" mac)])]
    [:a {:style {:cursor "pointer"} :on-click on-click} id]))


(defn nuvlabox-summary-table
  [{:keys [nuvlaboxStates] :as nb-info}]
  (let [rows (doall
               (for [{:keys [id updated nextCheck]} nuvlaboxStates]
                 ^{:key id}
                 [:tr
                  [:td (id-as-link id)]
                  [:td updated]
                  [:td nextCheck]]))]
    [:table (vec (concat [:tbody] rows))]))


(defn state-summary
  []
  (let [state-info (subscribe [::nuvlabox-subs/state-info])]
    (fn []
      (let [{:keys [stale active]} @state-info
            stale-count (:count stale)
            active-count (:count active)]
        [ui/Container {:fluid true}
         [cc/collapsible-card
          (str "stale nuvlabox machines (" stale-count ")")
          [nuvlabox-summary-table stale]]
         [cc/collapsible-card
          (str "active nuvlabox machines (" active-count ")")
          [nuvlabox-summary-table active]]]))))


(defn search-header []
  (let [tr (subscribe [::i18n-subs/tr])
        filter-visible? (subscribe [::nuvlabox-subs/filter-visible?])
        query-params (subscribe [::nuvlabox-subs/query-params])
        selected-id (subscribe [::nuvlabox-subs/collection-name])
        state-selector (subscribe [::nuvlabox-subs/state-selector])]
    (fn []
      ;; reset visible values of parameters
      (let [{:keys [$last $select]} @query-params]
        [ui/Form {:on-key-press #(when
                                   (and
                                     (= (.-charCode %) 13)  ; enter charcode = 13
                                     (some? @selected-id))
                                   ; blur active element in form to get last value in query-params
                                   (-> js/document .-activeElement .blur)
                                   (dispatch [::nuvlabox-events/get-results]))}

         (when @filter-visible?
           [ui/FormGroup

            [ui/FormField
             ; the key below is a workaround react issue with controlled input cursor position,
             ; this will force to re-render defaultValue on change of the value
             ^{:key (str "last:" $last)}
             [ui/Input {:type         "number"
                        :min          0
                        :label        (@tr [:last])
                        :defaultValue $last
                        :on-blur      #(dispatch
                                         [::nuvlabox-events/set-last (-> %1 .-target .-value)])}]]

            [ui/FormField
             ^{:key (str "state:" @state-selector)}
             [ui/Dropdown
              {:value     @state-selector
               :scrolling false
               :selection true
               :options   [{:value "all", :text "all states"}
                           {:value "new", :text "new state"}
                           {:value "activated", :text "activated state"}
                           {:value "quarantined", :text "quarantined state"}]
               :on-change (cutil/callback :value
                                          #(dispatch [::nuvlabox-events/set-state-selector %]))}]]])]))))


(defn format-field-item [selections-atom item]
  [ui/ListItem
   [ui/ListContent
    [ui/ListHeader
     [ui/Checkbox {:default-checked (contains? @selections-atom item)
                   :label           item
                   :on-change       identity #_(cutil/callback :checked
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
        available-fields (subscribe [::nuvlabox-subs/available-fields])
        selected-fields (subscribe [::nuvlabox-subs/selected-fields])
        selected-id (subscribe [::nuvlabox-subs/collection-name])
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
                       (dispatch [::nuvlabox-events/set-selected-fields @selections]))}
          (@tr [:update])]]]])))


(defn search-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::nuvlabox-subs/loading?])
        selected-id (subscribe [::nuvlabox-subs/collection-name])]
    (fn []
      [ui/MenuItem {:name     "search"
                    :disabled (nil? @selected-id)
                    :on-click #(dispatch [::nuvlabox-events/get-results])}
       (if @loading?
         [ui/Icon {:name    "refresh"
                   :loading @loading?}]
         [ui/Icon {:name "search"}])
       (@tr [:search])])))


(defn filter-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        filter-visible? (subscribe [::nuvlabox-subs/filter-visible?])]
    (fn []
      [ui/MenuMenu {:position "right"}
       [ui/MenuItem {:name     "filter"
                     :on-click #(dispatch [::nuvlabox-events/toggle-filter])}
        [ui/IconGroup
         [ui/Icon {:name "filter"}]
         [ui/Icon {:name   (if @filter-visible? "chevron down" "chevron right")
                   :corner true}]]
        (str "\u00a0" (@tr [:filter]))]])))


(defn menu-bar []
  (let [tr (subscribe [::i18n-subs/tr])
        resources (subscribe [::nuvlabox-subs/collection])]
    (fn []
      (when (instance? js/Error @resources)
        (dispatch [::messages-events/add
                   (let [{:keys [status message]} (response/parse-ex-info @resources)]
                     {:header  (cond-> (@tr [:error])
                                       status (str " (" status ")"))
                      :message message
                      :type    :error})]))
      [:div
       [ui/Menu {:attached   "top"
                 :borderless true}
        [refresh-button]
        [search-button]
        [select-fields]
        [filter-button]]
       [ui/Segment {:attached "bottom"}
        [search-header]]])))


(defn state-info
  []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (let [[_ mac] @path
            n (count @path)
            children (case n
                       1 [[menu-bar]
                          [state-summary]]
                       2 [[nuvlabox-detail/nb-detail]]
                       [[menu-bar]
                        [state-summary]])]
        (dispatch [::nuvlabox-detail-events/set-mac mac])
        (vec (concat [:div] children))))))


(defmethod panel/render :nuvlabox
  [path]
  [state-info])
