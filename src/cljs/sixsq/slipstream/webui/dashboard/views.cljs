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
    [sixsq.slipstream.webui.main.events :as main-events]
    ))


(defn as-statistic [{:keys [label value]}]
  [ui/Statistic
   [ui/StatisticValue value]
   [ui/StatisticLabel label]])

(defn vms-deployments []
  (let [selected-tab (subscribe [::dashboard-subs/selected-tab])
        visible? (subscribe [::main-subs/visible?])]
    (fn []
      (dispatch [::main-events/action-interval
                 (if (= @selected-tab 0)
                   {:action    :start
                    :id        :dashboard-deployments
                    :frequency 10000
                    :event     [::dashboard-events/get-virtual-machines]}
                   {:action    :start
                    :id        :dashboard-vms
                    :frequency 10000
                    :event     [::dashboard-events/get-virtual-machines]})])
      [ui/Tab
       {:onTabChange (fn [e, d]
                       (let [selected-tab (:activeIndex (js->clj d :keywordize-keys true))
                             action-id (if (= selected-tab 0) :dashboard-vms :dashboard-deployments)]
                         (dispatch [::main-events/action-interval {:action :end :id action-id}])
                         (dispatch [::dashboard-events/set-selected-tab selected-tab])))
        :panes       [{:menuItem "Deployments"
                       :render   (fn [] (r/as-element
                                          [:div {:style {:width "auto" :overflow-x "auto"}}
                                           [ui/TabPane {:as :div :style {:margin "10px"}}
                                            [:h2 "dep"]
                                            #_[dep/deployments-table]]
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
      [:div
       [:h1 (@tr [:dashboard])]
       [ui/Button
        {:circular true
         :primary  true
         :icon     "refresh"
         :loading  @loading?
         :on-click #(dispatch [::dashboard-events/get-statistics])}]
       (when-not @loading?
         (let [stats (->> @statistics
                          (sort-by :order)
                          (map as-statistic))]
           (vec (concat [:div] stats))))
       [vms-deployments]
       ])))


(defn ^:export setCloudFilter [cloud]
  (if (= cloud "All Clouds")
    (dispatch [::dashboard-subs/set-filtered-cloud nil])
    (dispatch [::dashboard-subs/set-filtered-cloud (str "connector/href=\"connector/" cloud "\"")])))

(defmethod panel/render :dashboard
  [path query-params]
  [dashboard-resource])
