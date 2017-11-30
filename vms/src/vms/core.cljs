(ns vms.core
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [sixsq.slipstream.client.api.cimi :as cimi]
            [soda-ash.core :as sa]
            [vms.visibility :as vs]
            [vms.client-utils :as req]
            [vms.deployments :as dep]
            [vms.vms :as vms]
            [clojure.string :as str]))

; TODO filter via cloud gauge
; Export component utils
; Done not refresh if windows not visible
; Done not refresh when other tab open
; Paging
; manage table content and size and enable scroll
; test integration with existing dashboard (left 2 issue, modal not centered and height body)
; color
; create helper components

(enable-console-print!)

(def app-state (atom {:web-page-visible true
                      :active-tab       0
                      :refresh          true}))

(defn state-set-web-page-visible [v]
  (swap! app-state assoc :web-page-visible v))

(defn state-set-refresh [v]
  (swap! app-state assoc :refresh v))

(defn state-set-active-tab [v]
  (swap! app-state assoc :active-tab v))

(vs/VisibleWebPage {:onWebPageVisible #(state-set-web-page-visible true)
                    :onWebPageHidden  #(state-set-web-page-visible false)})

(defn fetch-records []
  (when (and (get @app-state :web-page-visible) (get @app-state :refresh))
    (case (get @app-state :active-tab)
      0 (dep/fetch-deployments)
      1 (vms/fetch-vms)
      nil)))

(go (while true
      (js/console.log "refreshing!!!")
      (fetch-records)
      (<! (timeout 10000))))


(defn app []
  [sa/Tab
   {:onTabChange (fn [e, d]
                   (state-set-active-tab (.-activeIndex d))
                   (fetch-records))
    :panes       [{:menuItem "Deployments"
                   :render   (fn [] (reagent/as-element [:div {:style {:width "auto" :overflow-x "auto"}}
                                                         [sa/TabPane {:as :div :style {:margin "10px"}}
                                                          [dep/deployments-table]]
                                                         [:br]]))}
                  {:menuItem "Virtual Machines"
                   :render   (fn [] (reagent/as-element
                                      [:div {:style {:width "auto" :overflow-x "auto"}}
                                       [sa/TabPane {:as :div :style {:margin "10px"}} [vms/vms-table]]
                                       [:br]]))}
                  ]}])

(reagent/render-component [app]
                          (. js/document (getElementById "app")))
