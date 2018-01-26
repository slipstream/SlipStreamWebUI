(ns sixsq.slipstream.webui.deployment.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment.spec :as deployment-spec]))


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


