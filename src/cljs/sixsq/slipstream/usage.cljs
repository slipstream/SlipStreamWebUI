; TODO terms only return 10 element
; TODO right? division by 60 to get x hour

(ns sixsq.slipstream.usage
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [promesa.core :as p]
            [soda-ash.core :as sa]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [cljsjs.react-date-range]
            [cljsjs.moment]
            [cljs.pprint :as pprint]
            [sixsq.slipstream.legacy-components.utils.visibility :as vs]
            [sixsq.slipstream.legacy-components.utils.client :as client]
            [sixsq.slipstream.client.api.cimi :as cimi]))

;;
;; This option is not compatible with other platforms, notably nodejs.
;; Use instead the logging calls to provide console output.
;;
(enable-console-print!)

;;
;; debugging log level
;;
(log/set-level! :debug)                                     ; TODO not working call!

(def initial-start-date (.add (js/moment) -30 "days"))
(def initial-end-date (.add (js/moment) -1 "days"))
(def all-clouds "all-clouds")

(defn get-utc-created-date-start-str [d]
      (-> d js/moment .utc .format))

(defn get-utc-created-date-end-str [d]
      (-> d js/moment (.add 24 "hours") .utc .format))


(def app-state (atom {:web-page-visible true
                      :refresh          true
                      :connectors       {:list         []
                                         :selected     []
                                         :loading      true
                                         :request-opts {"$last"        0
                                                        "$aggregation" "terms:connector/href"}}
                      :request          {:created-after-utc  nil
                                         :created-before-utc nil}
                      :results          {:loading  true
                                         :metering {all-clouds  {}
                                                    :connectors []}}
                      }))

(defn state-set-web-page-visible [v]
      (swap! app-state assoc :web-page-visible v))

(defn state-set-refresh [v]
      (swap! app-state assoc :refresh v))

(defn state-set-connectors-list [v]
      (swap! app-state assoc-in [:connectors :list] v))

(defn state-set-connectors-selected [v]
      (swap! app-state assoc-in [:connectors :selected] v))

(defn state-set-connectors-loading [v]
      (swap! app-state assoc-in [:connectors :loading] v))

(defn state-set-results-loading [v]
      (swap! app-state assoc-in [:results :loading] v))

(defn state-set-request-created-after [v]
      (swap! app-state assoc-in [:request :created-after-utc] v))

(defn state-set-request-created-before [v]
      (swap! app-state assoc-in [:request :created-before-utc] v))

(defn state-set-results-all-clouds [v]
      (swap! app-state assoc-in [:results :metering all-clouds] v))

(defn state-set-results-connectors [v]
      (swap! app-state assoc-in [:results :metering :connectors] v))

(vs/VisibleWebPage :onWebPageVisible #(state-set-web-page-visible true)
                   :onWebPageHidden #(state-set-web-page-visible false))

(defn fetch-metering [resolve connector]
      (go
        (let [filter-created-str (str "created>'" (get-in @app-state [:request :created-after-utc])
                                      "' and created<'" (get-in @app-state [:request :created-before-utc]) "'")
              all-clouds? (= connector all-clouds)
              filter-str (str filter-created-str (when-not all-clouds? (str " and connector/href='" connector "'")))
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
      (when (and (get @app-state :web-page-visible) (get @app-state :refresh))
            (go
              (let [request-opts (get-in @app-state [:connectors :request-opts])
                    response (<! (cimi/search client/client "meterings" request-opts))
                    connectors (->> (get-in response [:aggregations :terms:connector/href :buckets] [])
                                    (map #(:key %)))]
                   (state-set-connectors-list connectors))
              (state-set-connectors-loading false)
              (fetch-meterings))))

(defn set-dates [calendar-data]
      (state-set-request-created-after (-> (.-startDate calendar-data) js/moment .utc .format))
      (state-set-request-created-before (-> (.-endDate calendar-data) js/moment (.add 24 "hours") .utc .format)))

(defn calendar []
      (js/React.createElement
        js/ReactDateRange.DateRange
        (clj->js {:onChange        set-dates
                  :ranges          {
                                    "Today"        {
                                                    :startDate (js/moment)
                                                    :endDate   (js/moment)
                                                    }
                                    "Last 7 days"  {
                                                    :startDate (.add (js/moment) -7 "days")
                                                    :endDate   (.add (js/moment) -1 "days")
                                                    }

                                    "Last 30 days" {
                                                    :startDate initial-start-date
                                                    :endDate   initial-end-date
                                                    }
                                    }
                  :onInit          set-dates
                  :calendars       2
                  :firstDayOfWeek  1
                  :startDate       initial-start-date
                  :endDate         initial-end-date
                  :minDate         (.add (js/moment) -90 "days")
                  :maxDate         (js/moment)
                  :linkedCalendars false})))

(defn search-header []
      [sa/Segment {:raised true}
       (calendar)
       [sa/Grid {:columns 2}
        [sa/GridColumn {:floated "left" :width 13}
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
                                       (conj
                                         (-> (js->clj %2 :keywordize-keys true) :value)
                                         all-clouds))
                       :options     (map
                                      #(let [connector-href %
                                             connector-name (str/replace connector-href #"^connector/" "")]
                                            {:key connector-href :value connector-href :text connector-name})
                                      (get-in @app-state [:connectors :list]))
                       }]]

        [sa/GridColumn {:floated "right" :textAlign "right" :width 3}
         [sa/Button {:icon    "search" :content "Search" :labelPosition "left"
                     :onClick #(do
                                 (state-set-results-loading true)
                                 (fetch-meterings))}]]]])


(defn format [fmt-str v]
      (pprint/cl-format nil fmt-str v))

(defn to-hour [v]
      (/ v 60))

(defn value-in-table [v]
      (->> v to-hour (format "~,2f")))

(defn value-in-statistic [v]
      (->> v to-hour Math/round (format "~,,'',3:d ")))

(defn search-result []
      (let [results (get @app-state :results)
            res-all-clouds (get-in results [:metering all-clouds] {})]
           [sa/Segment {:basic true :loading (:loading results)}
            [sa/Segment {:padded false :basic true :textAlign "center" :style {:margin 0 :padding 0}}
             [sa/Statistic {:size "tiny"}
              [sa/StatisticValue "ALL"]
              [sa/StatisticLabel {} "CLOUDS"]]
             [sa/Statistic {:size "tiny"}
              [sa/StatisticValue (value-in-statistic (:vms res-all-clouds))
               [sa/Icon {:name "server"}]]
              [sa/StatisticLabel {} "VM"]]
             [sa/Statistic {:size "tiny"}
              [sa/StatisticValue (value-in-statistic (:vcpu res-all-clouds))
               [sa/Icon {:size "small" :rotated "clockwise" :name "microchip"}]]
              [sa/StatisticLabel {} "CPU"]]
             [sa/Statistic {:size "tiny"}
              [sa/StatisticValue (value-in-statistic (:ram res-all-clouds))
               [sa/Icon {:size "small" :name "grid layout"}]]
              [sa/StatisticLabel {} "RAM"]]
             [sa/Statistic {:size "tiny"}
              [sa/StatisticValue (value-in-statistic (:disk res-all-clouds))
               [sa/Icon {:size "small" :name "database"}]]
              [sa/StatisticLabel {} "Disk"]]]
            [sa/Segment {:padded false :style {:margin 0 :padding 0} :basic true}
             [sa/Table {:striped true}
              [sa/TableHeader
               [sa/TableRow
                [sa/TableHeaderCell "Cloud"]
                [sa/TableHeaderCell {:textAlign "center"} "VMs" [:br] "[VM * h]"]
                [sa/TableHeaderCell {:textAlign "center"} "CPUs" [:br] "[vCPU * h]"]
                [sa/TableHeaderCell {:textAlign "center"} "RAM" [:br] "[GB * h]"]
                [sa/TableHeaderCell {:textAlign "center"} "DISK" [:br] "[GB * h]"]]]
              [sa/TableBody
               (map
                 #(do
                    ^{:key (:connector %)}
                    [sa/TableRow
                     [sa/TableCell (str/replace (:connector %) #"^connector/" "")]
                     [sa/TableCell {:textAlign "center"} (value-in-table (:vms %))]
                     [sa/TableCell {:textAlign "center"} (value-in-table (:vcpu %))]
                     [sa/TableCell {:textAlign "center"} (value-in-table (:ram %))]
                     [sa/TableCell {:textAlign "center"} (value-in-table (:disk %))]])
                 (get-in @app-state [:results :metering :connectors] []))]]]]))

(defn app []
      (js/console.log @app-state)
      [:div
       [search-header]
       [search-result]])

;;
;; hook to initialize the web application
;;

(defn ^:export init []
      (when-let [container-element (.getElementById js/document "usage-container")]
                (reagent/render-component [app] container-element)
                (fetch-connectors-list)))
