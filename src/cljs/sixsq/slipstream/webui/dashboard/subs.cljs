(ns sixsq.slipstream.webui.dashboard.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.dashboard.spec :as dashboard-spec]))


(reg-sub
  ::statistics
  (fn [db]
    (::dashboard-spec/statistics db)))


(reg-sub
  ::loading?
  (fn [db]
    (::dashboard-spec/loading? db)))

(reg-sub
  ::selected-tab
  (fn [db]
    (::dashboard-spec/selected-tab db)))

(reg-sub
  ::virtual-machines
  (fn [db]
    (::dashboard-spec/virtual-machines db)))

