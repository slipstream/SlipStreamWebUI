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
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.forms :as forms]
    [sixsq.slipstream.webui.utils.response :as response]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]
    [reagent.core :as reagent]))


(defn stat
  [value icon color label]
  [ui/Statistic {:size "tiny"}
   [ui/StatisticValue value "\u2002" [ui/Icon {:name icon :color color}]]
   [ui/StatisticLabel label]])


(defn health-summary
  []
  (let [results (subscribe [::nuvlabox-subs/collection])
        health-info (subscribe [::nuvlabox-subs/health-info])]
    (let [{:keys [stale-count active-count]} @health-info
          total (some->> @results :nuvlaboxRecords count)
          unknown (max (- total stale-count active-count) 0)]
      [ui/Segment style/evenly
       [stat total "computer" "black" "NuvlaBox"]
       [stat stale-count "warning sign" "red" "stale"]
       [stat active-count "check circle" "green" "active"]
       [stat unknown "ellipsis horizontal" "yellow" "unknown"]])))


(defn search-header []
  (let [tr (subscribe [::i18n-subs/tr])
        query-params (subscribe [::nuvlabox-subs/query-params])
        state-selector (subscribe [::nuvlabox-subs/state-selector])]
    (fn []
      ;; reset visible values of parameters
      (let [{:keys [$last $select]} @query-params]
        [ui/Form {:on-key-press
                  (partial forms/on-return-key
                           (fn []
                             (dispatch [::nuvlabox-events/fetch-health-info])
                             (dispatch [::nuvlabox-events/get-results])))}

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
                                             (dispatch [::nuvlabox-events/get-results])))}]]]]))))


(defn search-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::nuvlabox-subs/loading?])]
    (fn []
      [uix/MenuItemWithIcon
       {:name      (@tr [:refresh])
        :icon-name "refresh"
        :loading?  @loading?
        :disabled  false #_(nil? @selected-id)
        :on-click  (fn []
                     (dispatch [::nuvlabox-events/fetch-health-info])
                     (dispatch [::nuvlabox-events/get-results]))}])))


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
      [ui/Segment style/basic
       [ui/Menu {:attached   "top"
                 :borderless true}
        [search-button]]
       [ui/Segment {:attached "bottom"}
        [search-header]]])))


(defn format-nb-header
  []
  [ui/TableHeader
   [ui/TableRow
    [ui/TableHeaderCell [ui/Icon {:name "heartbeat"}]]
    [ui/TableHeaderCell "mac"]
    [ui/TableHeaderCell "state"]
    [ui/TableHeaderCell "name"]]])


(defn health-icon
  [value]
  (case value
    true [ui/Icon {:name "check circle" :color "green"}]
    false [ui/Icon {:name "warning sign" :color "red"}]
    [ui/Icon {:name "ellipsis horizontal" :color "yellow"}]))


(defn format-nb-row
  [healthy? {:keys [id macAddress state name] :as row}]
  (let [uuid (second (str/split id #"/"))
        on-click (fn []
                   (dispatch [::nuvlabox-detail-events/clear-detail])
                   (dispatch [::history-events/navigate (str "nuvlabox/" uuid)]))]
    [ui/TableRow {:on-click on-click
                  :style    {:cursor "pointer"}}
     [ui/TableCell {:collapsing true} (health-icon (get healthy? id))]
     [ui/TableCell {:collapsing true} [:a {:on-click on-click} macAddress]]
     [ui/TableCell {:collapsing true} state]
     [ui/TableCell name]]))


(defn nb-table
  []
  (let [results (subscribe [::nuvlabox-subs/collection])
        health-info (subscribe [::nuvlabox-subs/health-info])]
    (dispatch [::nuvlabox-events/get-results])
    (fn []
      (let [{:keys [healthy?]} @health-info
            data (some->> @results
                          :nuvlaboxRecords
                          (map #(select-keys % #{:id :macAddress :state :name})))]
        [:div
         [ui/Table {:compact "very", :selectable true}
          (format-nb-header)
          (vec (concat [ui/TableBody]
                       (mapv (partial format-nb-row healthy?) data)))]
         ]))))


(defn general-view []
  (let []
    (dispatch [::main-events/action-interval {:action    :start
                                              :id        :nuvlabox-health-info
                                              :frequency 15000
                                              :event     [::nuvlabox-events/fetch-health-info]}])
    [[menu-bar]
     [health-summary]
     [nb-table]]))


(defn nb-info
  []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (let [[_ mac] @path
            n (count @path)
            children (case n
                       1 (general-view)
                       2 [[nuvlabox-detail/nb-detail]]
                       (general-view))]
        (dispatch [::nuvlabox-detail-events/set-mac mac])
        (vec (concat [ui/Segment style/basic] children))))))


(defmethod panel/render :nuvlabox
  [path]
  [nb-info])
