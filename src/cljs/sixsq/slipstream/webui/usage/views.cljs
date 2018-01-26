(ns sixsq.slipstream.webui.usage.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [cljsjs.moment]
    [cljsjs.react-date-range]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.usage.subs :as usage-subs]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.usage.events :as usage-events]

    [clojure.string :as str]
    [cljs.pprint :as pprint]
    [taoensso.timbre :as log]))

(defn set-dates [calendar-data]
  (let [date-after (-> (.-startDate calendar-data) .clone)
        date-before (-> (.-endDate calendar-data) .clone (.add 1 "days") (.add -1 "seconds"))]
    (dispatch [::usage-events/set-date-after-before date-after date-before])))

(defn search-calendar []
  (let [date-before (subscribe [::usage-subs/date-before])
        date-after (subscribe [::usage-subs/date-after])
        initial-after-date (-> (js/moment) (.startOf "date") (.add -30 "days"))
        initial-before-date (-> (js/moment) (.startOf "date") (.add -1 "days"))]
    (fn []
      [ui/Container
       (js/React.createElement
         js/ReactDateRange.DateRange
         (clj->js {:onInit         set-dates
                   :onChange       set-dates
                   :ranges         {"Today"        {:startDate (-> (js/moment) (.startOf "date"))
                                                    :endDate   (-> (js/moment) (.startOf "date"))}
                                    "Yesterday"    {:startDate (-> (js/moment) (.startOf "date") (.add -1 "days"))
                                                    :endDate   (-> (js/moment) (.startOf "date") (.add -1 "days"))}
                                    "Last 7 days"  {:startDate (-> (js/moment) (.startOf "date") (.add -7 "days"))
                                                    :endDate   (-> (js/moment) (.startOf "date") (.add -1 "days"))}
                                    "Last 30 days" {:startDate (-> initial-after-date .clone)
                                                    :endDate   (-> initial-before-date .clone)}}
                   :calendars      2
                   :firstDayOfWeek 1
                   :startDate      (-> initial-after-date .clone)
                   :endDate        (-> initial-before-date .clone)
                   :minDate        (.add (js/moment) -90 "days")
                   :theme          (clj->js {:Calendar         {:width 200}
                                             :PredefinedRanges {:height 10 :width 100 :marginLeft 10 :marginTop 10}})
                   :maxDate        (js/moment)}))
       [ui/Label "From" [ui/LabelDetail (or (some-> @date-after
                                                    (.format "dddd, Do MMMM YYYY")) "-")]]
       [ui/Label "To" [ui/LabelDetail (or (some-> @date-before
                                                  (.format "dddd, Do MMMM YYYY")) "-")]]])))

(defn search-all-clouds-dropdown []
  (let [connectors-list (subscribe [::usage-subs/connectors-list])
        loading-connectors-list? (subscribe [::usage-subs/loading-connectors-list?])]
    (dispatch [::usage-events/get-connectors-list])
    (fn []
      [ui/Menu {:secondary true :size "small"}
       [ui/Dropdown {:fluid       true
                     :icon        "filter"
                     :className   "icon"
                     :labeled     true
                     :button      true
                     :placeholder "All clouds"
                     :loading     @loading-connectors-list?
                     :multiple    true
                     :search      true
                     :selection   true
                     :onChange    #(dispatch [::usage-events/set-selected-connectors
                                              (-> (js->clj %2 :keywordize-keys true) :value)])
                     :options     (map
                                    #(let [connector-href %
                                           connector-name (str/replace connector-href #"^connector/" "")]
                                       {:key connector-href :value connector-href :text connector-name})
                                    @connectors-list)}]])))

(defn search-users-dropdown []
  (let [selected-user (subscribe [::usage-subs/selected-user])
        users (subscribe [::usage-subs/users-list])
        loading? (subscribe [::usage-subs/loading-users-list?])]
    (fn []
      [ui/Menu {:secondary true :size "small"}
       [ui/Dropdown {:fluid       true
                     :placeholder "Filter by user"
                     :search      true
                     :icon        "users"
                     :labeled     true
                     :button      true
                     :value       @selected-user
                     :className   "icon"
                     :selection   true
                     :loading     @loading?
                     :onChange    #(dispatch [::usage-events/set-user (-> (js->clj %2 :keywordize-keys true) :value)])
                     :options     (map #(let [user-name (str/replace % #"^user/" "")]
                                          {:key user-name :value user-name :text user-name})
                                       @users)}]
       (when @selected-user
         [ui/MenuItem {:icon "close" :link true :onClick #(dispatch [::usage-events/clear-user])}])
       ])))

(defn search-header []
  (let [session (subscribe [::authn-subs/session])
        loading? (subscribe [::usage-subs/loading-users-list?])]
    (fn []
      (let [role (or (-> @session :roles (str/split #"\s+") first) "")
            is-admin? (= role "ADMIN")]
        [:div
         [ui/Segment {:size "mini"}
          [ui/Grid {:stackable true :columns 2}
           [ui/GridColumn {:width 10} [search-calendar]]
           [ui/GridColumn {:width 6 :stretched true}
            [ui/Segment {:basic true}
             [search-all-clouds-dropdown]
             (when is-admin?
               (when @loading?
                 (dispatch [::usage-events/get-users-list]))
               [search-users-dropdown])
             [:div
              [ui/Button {:icon    "search" :color "blue" :circular true :floated "right"
                          :onClick #(dispatch [::usage-events/fetch-meterings])}]]
             ]]]]
         ]))))

(defn format [fmt-str & v]
  (apply pprint/cl-format nil fmt-str v))

(defn to-hour [v]
  (/ v 60))

(defn value-in-table [v]
  (let [v-hour (to-hour v)
        v-int-part (int v-hour)
        v-float-part (- v-hour v-int-part)]
    (format "~,,'',3:d~0,2f" v-int-part v-float-part)))

(defn value-in-statistic [v]
  (->> v to-hour Math/round (format "~,,'',3:d ")))

(defn to-GB-from-MB [v]
  (/ v 1024))

(defn statistics-all-cloud []
  (let [results (subscribe [::usage-subs/results])]
    (fn []
      (let [res-all-clouds (:all-clouds @results)]
        [ui/Segment {:padded false :basic true :textAlign "center" :style {:margin 0 :padding 0}}
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue "ALL"]
          [ui/StatisticLabel "CLOUDS"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (:vms res-all-clouds))
           [ui/Icon {:name "server"}]]
          [ui/StatisticLabel "VMs"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (:vcpu res-all-clouds))
           [ui/Icon {:size "small" :rotated "clockwise" :name "microchip"}]]
          [ui/StatisticLabel "CPU"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (to-GB-from-MB (:ram res-all-clouds)))
           [ui/Icon {:size "small" :name "grid layout"}]]
          [ui/StatisticLabel "RAM"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (to-GB-from-MB (:disk res-all-clouds)))
           [ui/Icon {:size "small" :name "database"}]]
          [ui/StatisticLabel {} "Disk"]]]))))

(defn table-results-clouds []
  (let [results (subscribe [::usage-subs/results])]
    (fn []
      [ui/Segment {:padded false :style {:margin 0 :padding 0} :basic true}
       [ui/Table {:striped true :fixed true}
        [ui/TableHeader
         [ui/TableRow
          [ui/TableHeaderCell "Cloud"]
          [ui/TableHeaderCell {:textAlign "center"} "VMs" [:br] "[h]"]
          [ui/TableHeaderCell {:textAlign "center"} "CPUs" [:br] "[h]"]
          [ui/TableHeaderCell {:textAlign "center"} "RAM" [:br] "[GBh]"]
          [ui/TableHeaderCell {:textAlign "center"} "DISK" [:br] "[GBh]"]]]
        [ui/TableBody
         (map (fn [connector result]
                ^{:key (name connector)}
                [ui/TableRow
                 [ui/TableCell (str/replace (name connector) #"^connector/" "")]
                 [ui/TableCell {:textAlign "center"} (value-in-table (:vms result))]
                 [ui/TableCell {:textAlign "center"} (value-in-table (:vcpu result))]
                 [ui/TableCell {:textAlign "center"} (value-in-table (to-GB-from-MB (:ram result)))]
                 [ui/TableCell {:textAlign "center"} (value-in-table (to-GB-from-MB (:disk result)))]])
              (keys @results) (vals @results))]
        ]])))

(defn search-result []
  (let [loading? (subscribe [::usage-subs/loading?])
        results (subscribe [::usage-subs/results])]
    (fn []
      [ui/Segment {:basic true :loading @loading?}
       [statistics-all-cloud]
       [table-results-clouds]])))

(defn usage
  []
  (fn []
    [:div
     [search-header]
     [search-result]]))

(defmethod panel/render :usage
  [path query-params]
  [usage])
