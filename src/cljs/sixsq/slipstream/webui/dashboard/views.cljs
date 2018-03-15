(ns sixsq.slipstream.webui.dashboard.views
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    [reagent.core :as r]

    [cljs.core.async :refer [<! >! chan timeout]]

    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.dashboard.events :as dashboard-events]
    [sixsq.slipstream.webui.dashboard.subs :as dashboard-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.dashboard.views-vms :as vms]
    [sixsq.slipstream.webui.dashboard.views-deployments :as dep]
    [sixsq.slipstream.webui.main.events :as main-events]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]))


(defn as-statistic [{:keys [label value]}]
  ^{:key label}
  [ui/Statistic
   [ui/StatisticValue value]
   [ui/StatisticLabel label]])

(defn vms-deployments []
  (let [selected-tab (subscribe [::dashboard-subs/selected-tab])]
    (dispatch [::main-events/action-interval {:action    :start
                                              :id        :dashboard-tab
                                              :frequency 15000
                                              :event     [::dashboard-events/fetch-tab-records]}])
    (fn []
      [ui/Tab
       {:menu        {:attached true :tabular true :size "tiny"}
        :onTabChange (fn [e, d]
                       (let [activeTabIndex (:activeIndex (js->clj d :keywordize-keys true))
                             activeTab (case activeTabIndex
                                         0 "deployments"
                                         1 "virtual-machines")]
                         (dispatch [::dashboard-events/set-selected-tab activeTab])))
        :panes       [{:menuItem "Deployments"
                       :render   (fn [] (r/as-element
                                          [:div {:style {:width "auto" :overflow-x "auto"}}
                                           [ui/TabPane {:as :div :style {:margin "10px"}}
                                            [dep/deployments-table]]
                                           ]))}
                      {:menuItem "Virtual Machines"
                       :render   (fn [] (r/as-element
                                          [:div {:style {:width "auto" :overflow-x "auto"}}
                                           [ui/TabPane {:as :div :style {:margin "10px"}}
                                            [vms/vms-table]]
                                           ]))}]}])))


(defn dashboard-resource
  []
  (let [tr (subscribe [::i18n-subs/tr])
        statistics (subscribe [::dashboard-subs/statistics])
        loading? (subscribe [::dashboard-subs/loading?])]
    (fn []
      [ui/Container {:fluid true}
       [ui/Menu
        [ui/MenuItem {:name "refresh"
                      :on-click #(dispatch [::dashboard-events/get-statistics])}
         [ui/Icon {:name "refresh"
                   :loading @loading?}]
         (@tr [:refresh])]]
       (when-not @loading?
         (let [stats (->> @statistics
                          (sort-by :order)
                          (map as-statistic))]
           [cc/collapsible-card (@tr [:statistics]) [ui/StatisticGroup {:size :tiny} stats]]))
       [vms-deployments]])))

(defn ^:export set-cloud-filter [cloud]
  (log/debug "dispatch open-modal for authn view")
  (dispatch [::dashboard-events/set-filtered-cloud cloud]))

(defmethod panel/render :dashboard
  [path]
  [dashboard-resource])
