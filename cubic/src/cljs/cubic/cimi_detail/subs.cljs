(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.cimi-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [cubic.cimi-detail.spec :as cimi-detail-spec]))


(reg-sub
  ::loading?
  (fn [db]
    (::cimi-detail-spec/loading? db)))


(reg-sub
  ::resource-id
  (fn [db _]
    (::cimi-detail-spec/resource-id db)))


(reg-sub
  ::resource
  (fn [db _]
    (::cimi-detail-spec/resource db)))
