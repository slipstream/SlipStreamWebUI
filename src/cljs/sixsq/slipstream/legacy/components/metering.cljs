(ns sixsq.slipstream.legacy.components.metering
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<! >! chan timeout]]
            [promesa.core :as p]
            [soda-ash.core :as sa]
            [clojure.string :as str]
            [cljsjs.react-date-range]
            [cljsjs.moment]
            [cljs.pprint :as pprint]
            [taoensso.timbre :as log]
            [sixsq.slipstream.legacy.utils.client :as client]
            [sixsq.slipstream.client.api.cimi :as cimi]))

(def initial-start-date (-> (js/moment) (.startOf "date") (.add -30 "days")))
(def initial-end-date (-> (js/moment) (.startOf "date") (.add -1 "days")))
(def all-clouds "all-clouds")

(def app-state (r/atom {:is-admin   false
                        :connectors {:list     []
                                     :selected []
                                     :loading  true}
                        :users      {:list     []
                                     :selected nil
                                     :loading  true}
                        :request    {:created-after  nil
                                     :created-before nil}
                        :results    {:loading  true
                                     :metering {all-clouds  {}
                                                :connectors []}}
                        }))

(defn state-set-is-admin [v]
      (swap! app-state assoc :is-admin v))

(defn state-set-connectors-list [v]
      (swap! app-state assoc-in [:connectors :list] v))

(defn state-set-connectors-selected [v]
      (swap! app-state assoc-in [:connectors :selected] v))

(defn state-set-connectors-loading [v]
      (swap! app-state assoc-in [:connectors :loading] v))

(defn state-set-users-list [v]
      (swap! app-state assoc-in [:users :list] v))

(defn state-set-users-selected [v]
      (swap! app-state assoc-in [:users :selected] v))

(defn state-set-users-loading [v]
      (swap! app-state assoc-in [:users :loading] v))

(defn state-set-results-loading [v]
      (swap! app-state assoc-in [:results :loading] v))

(defn state-set-request-created-after [v]
      (swap! app-state assoc-in [:request :created-after] v))

(defn state-set-request-created-before [v]
      (swap! app-state assoc-in [:request :created-before] v))

(defn state-set-results-all-clouds [v]
      (swap! app-state assoc-in [:results :metering all-clouds] v))

(defn state-set-results-connectors [v]
      (swap! app-state assoc-in [:results :metering :connectors] v))

(defn fetch-users-list []
      (go
        (let [request-opts {"$select" "id"}
              response (<! (cimi/search client/client "users" request-opts))
              users (map #(:id %) (get response :users []))]
             (state-set-users-list users))
        (state-set-users-loading false)))

(defn handle-is-admin []
      (go
        (let [response (<! (cimi/search client/client "sessions"))
              role (or (-> response :sessions first :roles (str/split #"\s+") first) "")]
             (when (= role "ADMIN")
                   (state-set-is-admin true)
                   (fetch-users-list)))))

(defn fetch-metering [resolve connector]
      (go
        (let [filter-created-str (str "created>'"
                                      (-> @app-state (get-in [:request :created-after]) .clone .utc .format)
                                      "' and created<'"
                                      (-> @app-state (get-in [:request :created-before]) .clone .utc .format) "'")
              selected-user (get-in @app-state [:users :selected])
              filter-user-str (when selected-user (str "acl/rules/principal='" selected-user "'"))
              all-clouds? (= connector all-clouds)
              filter-connectors (when-not all-clouds? (str "connector/href='" connector "'"))
              filter-str (str/join " and " (remove str/blank? [filter-created-str filter-user-str filter-connectors]))
              request-opts {"$last"        0
                            "$filter"      filter-str
                            "$aggregation" (str "cardinality:instanceID, sum:serviceOffer/resource:vcpu, "
                                                "sum:serviceOffer/resource:ram, sum:serviceOffer/resource:disk")}
              response (<! (cimi/search client/client "meterings" request-opts))]
             (resolve
               {:connector connector
                :vms       (get-in response [:aggregations :cardinality:instanceID :value] 0)
                :vcpu      (get-in response [:aggregations :sum:serviceOffer/resource:vcpu :value] 0)
                :ram       (get-in response [:aggregations :sum:serviceOffer/resource:ram :value] 0)
                :disk      (get-in response [:aggregations :sum:serviceOffer/resource:disk :value] 0)})
             )))

(defn fetch-metering-all-cloud []
      (-> (p/promise (fn [resolve _] (fetch-metering resolve all-clouds)))
          (p/then #(state-set-results-all-clouds %))))

(defn fetch-meterings []
      (fetch-metering-all-cloud)
      (let [selected-connectors (get-in @app-state [:connectors :selected])
            connectors (if (empty? selected-connectors)
                         (get-in @app-state [:connectors :list])
                         selected-connectors)
            p (p/all (map #(p/promise (fn [resolve _] (fetch-metering resolve %))) connectors))]
           (p/then p #(do (state-set-results-connectors %)
                          (state-set-results-loading false)))))

(defn fetch-connectors-list []
      (go
        (let [request-opts {"$last"        0
                            "$aggregation" "terms:connector/href"}
              response (<! (cimi/search client/client "meterings" request-opts))
              connectors (->> (get-in response [:aggregations :terms:connector/href :buckets] [])
                              (map #(:key %)))]
             (state-set-connectors-list connectors))
        (state-set-connectors-loading false)
        (fetch-meterings)))

(defn set-dates [calendar-data]
      (state-set-request-created-after (-> (.-startDate calendar-data) .clone))
      (state-set-request-created-before (-> (.-endDate calendar-data) .clone (.add 1 "days") (.add -1 "seconds"))))

(defn search-calendar []
      [sa/Container
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
                                    "Last 30 days" {:startDate (-> initial-start-date .clone)
                                                    :endDate   (-> initial-end-date .clone)}}
                   :calendars      2
                   :firstDayOfWeek 1
                   :startDate      (-> initial-start-date .clone)
                   :endDate        (-> initial-end-date .clone)
                   :minDate        (.add (js/moment) -90 "days")
                   :theme          (clj->js {:Calendar         {:width 200}
                                             :PredefinedRanges {:height 10 :width 100 :marginLeft 10 :marginTop 10}})
                   :maxDate        (js/moment)}))
       [sa/Label "From" [sa/LabelDetail (or (some-> (get-in @app-state [:request :created-after])
                                                    (.format "dddd, Do MMMM YYYY")) "-")]]
       [sa/Label "To" [sa/LabelDetail (or (some-> (get-in @app-state [:request :created-before])
                                                  (.format "dddd, Do MMMM YYYY")) "-")]]])

(defn search-all-clouds-dropdown []
  [sa/Menu {:secondary true :size "small"}
   [sa/Dropdown {:fluid       true
                 :icon        "filter"
                 :className   "icon"
                 :labeled     true
                 :button      true
                 :placeholder "All clouds"
                 :loading     (get-in @app-state [:connectors :loading])
                 :multiple    true
                 :search      true
                 :selection   true
                 :onChange    #(state-set-connectors-selected
                                 (-> (js->clj %2 :keywordize-keys true) :value))
                 :options     (map
                                #(let [connector-href %
                                       connector-name (str/replace connector-href #"^connector/" "")]
                                   {:key connector-href :value connector-href :text connector-name})
                                (get-in @app-state [:connectors :list]))
                 }]])

(defn search-users-dropdown []
      (let [selected-user (get-in @app-state [:users :selected])]
           [sa/Menu {:secondary true :size "small"}
            [sa/Dropdown {:fluid       true
                          :placeholder "Filter by user"
                          :search      true
                          :icon        "users"
                          :labeled     true
                          :button      true
                          :value       selected-user
                          :className   "icon"
                          :selection   true
                          :loading     (get-in @app-state [:users :loading])
                          :onChange    #(state-set-users-selected (-> (js->clj %2 :keywordize-keys true) :value))
                          :options     (map #(let [user-name (str/replace % #"^user/" "")]
                                               {:key user-name :value user-name :text user-name})
                                            (get-in @app-state [:users :list]))}]
            (when (get-in @app-state [:users :selected])
              [sa/MenuItem {:icon "close" :link true :onClick #(state-set-users-selected nil)}])
            ]))

(defn search-header []
      [sa/Segment {:size "mini"}
       [sa/Grid {:stackable true :columns 2}
        [sa/GridColumn {:width 10} (search-calendar)]
        [sa/GridColumn {:width 6 :stretched true}
         [sa/Segment {:basic true}
          [search-all-clouds-dropdown]
          (when (get @app-state :is-admin)
            [search-users-dropdown])]
         [sa/Segment {:basic true :size "small"}
          [sa/Label {:as :a :icon "search" :content "Search" :attached "bottom right" :size "medium"
                     :onClick #(do
                                 (state-set-results-loading true)
                                 (fetch-meterings))}]]]]])

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
      (let [res-all-clouds (get-in @app-state [:results :metering all-clouds] {})]
           [sa/Segment {:padded false :basic true :textAlign "center" :style {:margin 0 :padding 0}}
            [sa/Statistic {:size "tiny"}
             [sa/StatisticValue "ALL"]
             [sa/StatisticLabel "CLOUDS"]]
            [sa/Statistic {:size "tiny"}
             [sa/StatisticValue (value-in-statistic (:vms res-all-clouds))
              [sa/Icon {:name "server"}]]
             [sa/StatisticLabel "VMs"]]
            [sa/Statistic {:size "tiny"}
             [sa/StatisticValue (value-in-statistic (:vcpu res-all-clouds))
              [sa/Icon {:size "small" :rotated "clockwise" :name "microchip"}]]
             [sa/StatisticLabel "CPU"]]
            [sa/Statistic {:size "tiny"}
             [sa/StatisticValue (value-in-statistic (to-GB-from-MB (:ram res-all-clouds)))
              [sa/Icon {:size "small" :name "grid layout"}]]
             [sa/StatisticLabel "RAM"]]
            [sa/Statistic {:size "tiny"}
             [sa/StatisticValue (value-in-statistic (to-GB-from-MB (:disk res-all-clouds)))
              [sa/Icon {:size "small" :name "database"}]]
             [sa/StatisticLabel {} "Disk"]]]))

(defn table-results-clouds []
      [sa/Segment {:padded false :style {:margin 0 :padding 0} :basic true}
       [sa/Table {:striped true :fixed true}
        [sa/TableHeader
         [sa/TableRow
          [sa/TableHeaderCell "Cloud"]
          [sa/TableHeaderCell {:textAlign "center"} "VMs" [:br] "[h]"]
          [sa/TableHeaderCell {:textAlign "center"} "CPUs" [:br] "[h]"]
          [sa/TableHeaderCell {:textAlign "center"} "RAM" [:br] "[GBh]"]
          [sa/TableHeaderCell {:textAlign "center"} "DISK" [:br] "[GBh]"]]]
        [sa/TableBody
         (map
           #(do
              ^{:key (:connector %)}
              [sa/TableRow
               [sa/TableCell (str/replace (:connector %) #"^connector/" "")]
               [sa/TableCell {:textAlign "center"} (value-in-table (:vms %))]
               [sa/TableCell {:textAlign "center"} (value-in-table (:vcpu %))]
               [sa/TableCell {:textAlign "center"} (value-in-table (to-GB-from-MB (:ram %)))]
               [sa/TableCell {:textAlign "center"} (value-in-table (to-GB-from-MB (:disk %)))]])
           (get-in @app-state [:results :metering :connectors] []))]]])

(defn search-result []
      (let [results (get @app-state :results)]
           [sa/Segment {:basic true :loading (get-in @app-state [:results :loading])}
            [statistics-all-cloud]
            [table-results-clouds]]))

(defn app []
      #_(log/debug @app-state)
      [:div
       [search-header]
       [search-result]])

;;
;; hook to initialize the web application
;;

(defn init [container]
      (r/render [app] container)
      (handle-is-admin)
      (fetch-connectors-list))
