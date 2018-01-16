(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.dashboard.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [cubic.dashboard.spec :as dashboard-spec]))


(reg-sub
  ::statistics
  (fn [db]
    (::dashboard-spec/statistics db)))


(reg-sub
  ::loading?
  (fn [db]
    (::dashboard-spec/loading? db)))


