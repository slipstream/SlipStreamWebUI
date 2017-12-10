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
            [goog.string :as gstring]
            [goog.string.format]
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

(def initial-start-date (.add (js/moment) -7 "days"))
(def initial-end-date (.add (js/moment) -1 "days"))

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
                      :result-table     {:loading  false
                                         :metering []}
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

(defn state-set-request-created-after [v]
      (swap! app-state assoc-in [:request :created-after-utc] v))

(defn state-set-request-created-before [v]
      (swap! app-state assoc-in [:request :created-before-utc] v))

(defn state-set-result-table-metering [v]
      (swap! app-state assoc-in [:result-table :metering] v))

(defn state-set-result-table-loading [v]
      (swap! app-state assoc-in [:result-table :loading] v))

(vs/VisibleWebPage :onWebPageVisible #(state-set-web-page-visible true)
                   :onWebPageHidden #(state-set-web-page-visible false))

(defn fetch-connectors-list []
      (when (and (get @app-state :web-page-visible) (get @app-state :refresh))
            (go
              (let [request-opts (get-in @app-state [:connectors :request-opts])
                    response (<! (cimi/search client/client "meterings" request-opts))
                    connectors (->> (get-in response [:aggregations :terms:connector/href :buckets] [])
                                    (map #(:key %)))]
                   (state-set-connectors-list connectors))
              (state-set-connectors-loading false))))

(defn fetch-metering [resolve connector]
      (go
        (let [filter-created-str (str "created>'" (get-in @app-state [:request :created-after-utc])
                                      "' and created<'" (get-in @app-state [:request :created-before-utc]) "'")
              filter-str (str filter-created-str " and connector/href='" connector "'")
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

(defn fetch-meterings []
      (let [selected-connectors (get-in @app-state [:connectors :selected])
            connectors (if (empty? selected-connectors)
                         (get-in @app-state [:connectors :list])
                         selected-connectors)
            p (p/all (map #(p/promise (fn [resolve _] (fetch-metering resolve %))) connectors))]
           (p/then p #(do (state-set-result-table-metering %)
                          (state-set-result-table-loading false)))))

(defn set-dates [calendar-data]
      (state-set-request-created-after (-> (.-startDate calendar-data) js/moment .utc .format))
      (state-set-request-created-before (-> (.-endDate calendar-data) js/moment (.add 24 "hours") .utc .format)))

(defn app []
      (js/console.log @app-state)
      [:div
       [:h1 "usage"]
       [sa/Segment {:clearing true}
        [sa/Form
         [sa/FormGroup {:widths 2}
          [sa/FormField (js/React.createElement
                          js/ReactDateRange.DateRange
                          (clj->js {:onChange        set-dates
                                    :ranges          {
                                                      "Today"        {
                                                                      :startDate (js/moment)
                                                                      :endDate   (js/moment)
                                                                      }
                                                      "Last 7 days"  {
                                                                      :startDate initial-start-date
                                                                      :endDate   initial-end-date
                                                                      }

                                                      "Last 30 days" {
                                                                      :startDate (.add (js/moment) -30 "days")
                                                                      :endDate   (.add (js/moment) -1 "days")
                                                                      }
                                                      }
                                    :onInit          set-dates
                                    :calendars       1
                                    :firstDayOfWeek  1
                                    :startDate       initial-start-date
                                    :endDate         initial-end-date
                                    :minDate         (.add (js/moment) -90 "days")
                                    :maxDate         (js/moment)
                                    :linkedCalendars false}))]
          [sa/FormField
           [sa/Dropdown {:icon        "filter"
                         :className   "icon"
                         :floating    true
                         :labeled     true
                         :button      true
                         :placeholder "All clouds"
                         :loading     (get-in @app-state [:connectors :loading])
                         :multiple    true
                         :search      true
                         :selection   true
                         :onChange    #(state-set-connectors-selected
                                         (-> (js->clj %2 :keywordize-keys true) :value))
                         :fluid       false
                         :options     (map
                                        #(let [connector-href %
                                               connector-name (str/replace connector-href #"^connector/" "")]
                                              {:key connector-href :value connector-href :text connector-name})
                                        (get-in @app-state [:connectors :list]))
                         }]]]
         [sa/FormField [sa/Button {:icon    "search" :circular true :floated "right"
                                   :onClick #(do
                                               (state-set-result-table-loading true)
                                               (fetch-meterings))}]]
         ]
        ]
       [sa/Segment {:basic true :loading (get-in @app-state [:result-table :loading])}
        [sa/Table {:striped true}
         [sa/TableHeader
          [sa/TableRow
           [sa/TableHeaderCell "Cloud"]
           [sa/TableHeaderCell {:textAlign "center"} "VMs" [:br] "(VM * h)"]
           [sa/TableHeaderCell {:textAlign "center"} "vCPU" [:br] "(vCPU * h)"]
           [sa/TableHeaderCell {:textAlign "center"} "Ram" [:br] "(GB * h)"]
           [sa/TableHeaderCell {:textAlign "center"} "Disk" [:br] "(GB * h)"]]]
         [sa/TableBody
          (map #(do
                  ^{:key (:connector %)} [sa/TableRow
                                          [sa/TableCell (:connector %)]
                                          [sa/TableCell {:textAlign "center"} (gstring/format "%.2f" (/ (:vms %) 60))]
                                          [sa/TableCell {:textAlign "center"} (gstring/format "%.2f" (/ (:vcpu %) 60))]
                                          [sa/TableCell {:textAlign "center"} (gstring/format "%.2f" (/ (:ram %) 60))]
                                          [sa/TableCell {:textAlign "center"} (gstring/format "%.2f" (/ (:disk %) 60))]
                                          ])
               (get-in @app-state [:result-table :metering]))]
         ]]])

;;
;; hook to initialize the web application
;;

(defn ^:export init []
      (when-let [container-element (.getElementById js/document "usage-container")]
                (reagent/render-component [app] container-element)
                (fetch-connectors-list)))
