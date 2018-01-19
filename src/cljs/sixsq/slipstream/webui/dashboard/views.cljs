(ns sixsq.slipstream.webui.dashboard.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.dashboard.events :as dashboard-events]
    [sixsq.slipstream.webui.dashboard.subs :as dashboard-subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]

    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


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
