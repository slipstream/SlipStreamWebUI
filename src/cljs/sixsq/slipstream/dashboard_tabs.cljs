(ns sixsq.slipstream.dashboard_tabs
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [soda-ash.core :as sa]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [sixsq.slipstream.legacy-components.utils.visibility :as vs]
            [sixsq.slipstream.legacy-components.utils.client :as client]
            [sixsq.slipstream.dashboard-tabs.deployments :as dep]
            [sixsq.slipstream.dashboard-tabs.vms :as vms]))

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
                      :active-tab       0
                      :refresh          true}))

(defn state-set-web-page-visible [v]
      (swap! app-state assoc :web-page-visible v))

(defn state-set-refresh [v]
      (swap! app-state assoc :refresh v))

(defn state-set-active-tab [v]
      (swap! app-state assoc :active-tab v))

(vs/VisibleWebPage :onWebPageVisible #(state-set-web-page-visible true)
                   :onWebPageHidden #(state-set-web-page-visible false))

(defn fetch-records []
      (when (and (get @app-state :web-page-visible) (get @app-state :refresh))
            (case (get @app-state :active-tab)
                  0 (dep/fetch-deployments)
                  1 (vms/fetch-vms)
                  nil)))

(go (while true
           (fetch-records)
           (<! (timeout 10000))))


(defn app []
      [sa/Tab
       {:onTabChange (fn [e, d]
                         (state-set-active-tab (:activeIndex (js->clj d :keywordize-keys true)))
                         (fetch-records)
                         (log/info @app-state))
        :panes       [{:menuItem "Deployments"
                       :render   (fn [] (reagent/as-element
                                          [:div {:style {:width "auto" :overflow-x "auto"}}
                                           [sa/TabPane {:as :div :style {:margin "10px"}}
                                            [dep/deployments-table]]
                                           [:br]]))}
                      {:menuItem "Virtual Machines"
                       :render   (fn [] (reagent/as-element
                                          [:div {:style {:width "auto" :overflow-x "auto"}}
                                           [sa/TabPane {:as :div :style {:margin "10px"}}
                                            [vms/vms-table]]
                                           [:br]]))}]}])

;;
;; hook to initialize the web application
;;
(defn ^:export init []
      (when-let [container-element (.getElementById js/document "dashboard-tabs-container")]
                (reagent/render-component [app] container-element)))
