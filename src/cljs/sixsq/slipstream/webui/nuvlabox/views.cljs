(ns sixsq.slipstream.webui.nuvlabox.views
  (:require
    [cljs.pprint :refer [cl-format pprint]]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.nuvlabox-detail.events :as nuvlabox-detail-events]
    [sixsq.slipstream.webui.nuvlabox-detail.views :as nuvlabox-detail]
    [sixsq.slipstream.webui.nuvlabox.events :as nuvlabox-events]
    [sixsq.slipstream.webui.nuvlabox.subs :as nuvlabox-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.response :as response]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))


(defn health-summary
  []
  (let [health-info (subscribe [::nuvlabox-subs/health-info])]
    (fn []
      (let [{:keys [stale-count active-count]} @health-info]
        [ui/Segment
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue stale-count]
          [ui/StatisticLabel "stale"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue active-count]
          [ui/StatisticLabel "active"]]]))))


(defn search-header []
  (let [tr (subscribe [::i18n-subs/tr])
        filter-visible? (subscribe [::nuvlabox-subs/filter-visible?])
        query-params (subscribe [::nuvlabox-subs/query-params])
        state-selector (subscribe [::nuvlabox-subs/state-selector])]
    (fn []
      ;; reset visible values of parameters
      (let [{:keys [$last $select]} @query-params]
        [ui/Form {:on-key-press #(when (= (.-charCode %) 13)
                                   ; blur active element in form to get last value in query-params
                                   (-> js/document .-activeElement .blur)
                                   (dispatch [::nuvlabox-events/fetch-health-info])
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
                        :on-blur      (ui-callback/input-callback
                                        #(dispatch [::nuvlabox-events/set-last %]))}]]

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
               :on-change (ui-callback/value (fn [value]
                                               (dispatch [::nuvlabox-events/set-state-selector value])
                                               (dispatch [::nuvlabox-events/fetch-health-info])
                                               (dispatch [::nuvlabox-events/get-results])))}]]])]))))


(defn search-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::nuvlabox-subs/loading?])]
    (fn []
      [ui/MenuItem {:name     "search"
                    :disabled false #_(nil? @selected-id)
                    :on-click (fn []
                                (dispatch [::nuvlabox-events/fetch-health-info])
                                (dispatch [::nuvlabox-events/get-results]))}
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
        [search-button]
        [filter-button]]
       [ui/Segment {:attached "bottom"}
        [search-header]]])))


(defn format-nb-link
  [id mac]
  (let [uuid (second (str/split id #"/"))]
    [:a {:on-click (fn []
                     (dispatch [::nuvlabox-detail-events/clear-detail])
                     (dispatch [::history-events/navigate (str "nuvlabox/" uuid)]))} mac]))


(defn format-nb-header
  []
  [ui/TableHeader
   [ui/TableRow
    [ui/TableHeaderCell [ui/Icon {:name "heartbeat", :size "large"}]]
    [ui/TableHeaderCell "mac"]
    [ui/TableHeaderCell "state"]
    [ui/TableHeaderCell "name"]]])


(defn health-icon
  [value]
  (case value
    true [ui/Icon {:name "check", :size :large}]
    false [ui/Icon {:name "warning sign", :size :large}]
    [ui/Icon {:name "ellipsis horizontal", :size :large}]))


(defn format-nb-row
  [healthy? {:keys [id macAddress state name] :as row}]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (health-icon (get healthy? id))]
   [ui/TableCell {:collapsing true} (format-nb-link id macAddress)]
   [ui/TableCell {:collapsing true} state]
   [ui/TableCell name]])


(defn nb-table
  []
  (let [results (subscribe [::nuvlabox-subs/collection])
        health-info (subscribe [::nuvlabox-subs/health-info])]
    (fn []
      (let [{:keys [healthy?]} @health-info
            data (some->> @results
                          :nuvlaboxRecords
                          (map #(select-keys % #{:id :macAddress :state :name})))]
        (vec (concat [ui/Table
                      (format-nb-header)]
                     (mapv (partial format-nb-row healthy?) data)))))))


(defn nb-info
  []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (let [[_ mac] @path
            n (count @path)
            children (case n
                       1 [[menu-bar]
                          [health-summary]
                          [nb-table]]
                       2 [[nuvlabox-detail/nb-detail]]
                       [[menu-bar]
                        [health-summary]
                        [nb-table]])]
        (dispatch [::nuvlabox-detail-events/set-mac mac])
        (vec (concat [:div] children))))))


(defmethod panel/render :nuvlabox
  [path]
  [nb-info])
