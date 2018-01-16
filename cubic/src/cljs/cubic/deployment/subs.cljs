(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.deployment.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [cubic.deployment.spec :as deployment-spec]))


(reg-sub
  ::loading?
  (fn [db]
    (::deployment-spec/loading? db)))


(reg-sub
  ::query-params
  (fn [db]
    (::deployment-spec/query-params db)))


(reg-sub
  ::deployments
  (fn [db]
    (::deployment-spec/deployments db)))


