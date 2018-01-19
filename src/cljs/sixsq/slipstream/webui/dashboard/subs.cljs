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


