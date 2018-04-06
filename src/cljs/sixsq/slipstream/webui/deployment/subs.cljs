(ns sixsq.slipstream.webui.deployment.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment.spec :as deployment-spec]))


(reg-sub
  ::loading?
  (fn [db]
    (::deployment-spec/loading? db)))


(reg-sub
  ::filter-visible?
  (fn [db]
    (::deployment-spec/filter-visible? db)))


(reg-sub
  ::query-params
  (fn [db]
    (::deployment-spec/query-params db)))


(reg-sub
  ::deployments
  (fn [db]
    (::deployment-spec/deployments db)))


(reg-sub
  ::deployment-target
  (fn [db]
    (::deployment-spec/deployment-target db)))


(reg-sub
  ::user-connectors-loading?
  (fn [db]
    (::deployment-spec/user-connectors-loading? db)))


(reg-sub
  ::user-connectors
  (fn [db]
    (::deployment-spec/user-connectors db)))


(reg-sub
  ::place-and-rank-loading?
  (fn [db]
    (::deployment-spec/place-and-rank-loading? db)))


(reg-sub
  ::place-and-rank
  (fn [db]
    (::deployment-spec/place-and-rank db)))
