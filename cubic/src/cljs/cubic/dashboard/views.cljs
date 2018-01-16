(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.dashboard.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    [cubic.panel :as panel]

    [cubic.dashboard.events :as dashboard-events]
    [cubic.dashboard.subs :as dashboard-subs]
    [cubic.i18n.subs :as i18n-subs]

    [cubic.utils.semantic-ui :as ui]))


(defn as-statistic [{:keys [label value]}]
  [ui/Statistic
   [ui/StatisticValue value]
   [ui/StatisticLabel label]])


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
           (vec (concat [:div] stats))))])))


(defmethod panel/render :dashboard
  [path query-params]
  [dashboard-resource])
