(ns sixsq.slipstream.usage
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [soda-ash.core :as sa]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [cljsjs.react-date-range]
            [cljsjs.moment]
            [sixsq.slipstream.legacy-components.utils.visibility :as vs]
            [sixsq.slipstream.legacy-components.utils.client :as client]
            [sixsq.slipstream.dashboard-tabs.deployments :as dep]
            [sixsq.slipstream.dashboard-tabs.vms :as vms]
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

(def app-state (atom {:web-page-visible true
                      :refresh          true
                      :connectors       {:list         []
                                         :loading      true
                                         :filtering    false
                                         :request-opts {"$last"        0
                                                        "$aggregation" "terms:connector/href"}}
                      :startDate        (.utc js/moment)
                      :endDate          (.utc js/moment)
                      }))

(defn state-set-web-page-visible [v]
  (swap! app-state assoc :web-page-visible v))

(defn state-set-refresh [v]
  (swap! app-state assoc :refresh v))

(defn state-set-start-date [v]
  (swap! app-state assoc :startDate v))

(defn state-set-end-date [v]
  (swap! app-state assoc :endDate v))

(defn state-set-connectors-list [v]
  (swap! app-state assoc-in [:connectors :list] v))

(defn state-set-connectors-filtering [v]
  (swap! app-state assoc-in [:connectors :filtering] v))

(vs/VisibleWebPage :onWebPageVisible #(state-set-web-page-visible true)
                   :onWebPageHidden #(state-set-web-page-visible false))

(defn fetch-connectors-list []
  (when (and (get @app-state :web-page-visible) (get @app-state :refresh))
    (go
      (let [response (<! (cimi/search client/client "meterings" (get-in @app-state [:connectors :request-opts])))]
        (state-set-connectors-list
          (conj (->>
                  (-> response
                      (get-in [:aggregations :terms:connector/href :buckets] []))
                  (map #(let [connector-href (:key %)
                              connector-name (str/replace connector-href #"^connector/" "")]
                          {:key connector-href :value connector-href :text connector-name})))
                {:key "All Connectors" :value "All Connectors" :text "All Connectors"}))
        ))
    ))

#_(go (while true
        (fetch-records)
        (<! (timeout 10000))))

(fetch-connectors-list)

(defn app []
  (js/console.log @app-state)
  [:h1 "usage"]
  [sa/Segment {:inverted true :clearing true}
   [sa/Form {:inverted true}
    [sa/FormGroup {:widths 2}
     [sa/FormField (js/React.createElement
                     js/ReactDateRange.DateRange
                     (clj->js {:onChange        #(js/console.log %)
                               ;:date            (.add (.utc js/moment) -7 "days")
                               :ranges          {
                                                 "Today"      {
                                                               :startDate (js/moment)
                                                               :endDate   (js/moment) ;(.add (js/moment) 8 "days")
                                                               }
                                                 "Last Week"  {
                                                               :startDate (.add (js/moment) -7 "days")
                                                               :endDate   (.add (js/moment) -1 "days")
                                                               }

                                                 "Last Month" {
                                                               :startDate (.add (js/moment) -30 "days")
                                                               :endDate   (.add (js/moment) -1 "days")
                                                               }
                                                 }
                               :calendars       1
                               :firstDayOfWeek  1
                               :maxDate         (js/moment)
                               :linkedCalendars false}))]
     [sa/FormField
      [sa/Dropdown {:icon        "filter"
                    :className   "icon"
                    :floating    true
                    :labeled     true
                    :button      true
                    :placeholder "All clouds"
                    :multiple    true
                    :search      true
                    :selection   true
                    :fluid       false
                    :options     (get-in @app-state [:connectors :list])}]]]
    [sa/FormField
     [sa/Button {:icon "search" :circular true :floated "right"}]]

    ]]
  )

;;
;; hook to initialize the web application
;;
(defn ^:export init []
  (when-let [container-element (.getElementById js/document "usage-container")]
    (reagent/render-component [app] container-element)))
