(ns sixsq.slipstream.legacy.components.dashboard_tabs
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<! >! chan timeout]]
            [soda-ash.core :as sa]
            [clojure.string :as str]
            [sixsq.slipstream.legacy.utils.visibility :as vs]
            [sixsq.slipstream.legacy.utils.client :as client]
            [sixsq.slipstream.legacy.components.dashboard-tabs.deployments :as dep]
            [sixsq.slipstream.legacy.components.dashboard-tabs.vms :as vms]))

(def app-state (r/atom {:web-page-visible true
                        :active-tab       0
                        :refresh          true}))

(defn state-set-web-page-visible [v]
      (swap! app-state assoc :web-page-visible v))

(defn state-set-refresh [v]
      (swap! app-state assoc :refresh v))

(defn state-set-active-tab [v]
      (swap! app-state assoc :active-tab v))

(defn fetch-records []
      (when (and (get @app-state :web-page-visible) (get @app-state :refresh))
            (case (get @app-state :active-tab)
                  0 (dep/fetch-deployments)
                  1 (vms/fetch-vms)
                  nil)))

(defn app []
      (js/console.log @app-state)
      [sa/Tab
       {:onTabChange (fn [e, d]
                         (state-set-active-tab (:activeIndex (js->clj d :keywordize-keys true)))
                         (fetch-records))
        :panes       [{:menuItem "Deployments"
                       :render   (fn [] (r/as-element
                                          [:div {:style {:width "auto" :overflow-x "auto"}}
                                           [sa/TabPane {:as :div :style {:margin "10px"}}
                                            [dep/deployments-table]]
                                           [:br]]))}
                      {:menuItem "Virtual Machines"
                       :render   (fn [] (r/as-element
                                          [:div {:style {:width "auto" :overflow-x "auto"}}
                                           [sa/TabPane {:as :div :style {:margin "10px"}}
                                            [vms/vms-table]]
                                           [:br]]))}]}])

;;
;; hook to initialize the web application
;;
(defn init [container]
      (r/render [app] container)
      (vs/VisibleWebPage :onWebPageVisible #(state-set-web-page-visible true)
                         :onWebPageHidden #(state-set-web-page-visible false))
      (go (while true
                 (fetch-records)
                 (<! (timeout 10000)))))
