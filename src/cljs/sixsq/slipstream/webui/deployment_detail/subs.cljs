(ns sixsq.slipstream.webui.deployment-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment-detail.spec :as deployment-detail-spec]))


(reg-sub
  ::runUUID
  (fn [db]
    (::deployment-detail-spec/runUUID db)))

(reg-sub
  ::reports
  ::deployment-detail-spec/reports)

(reg-sub
  ::loading?
  ::deployment-detail-spec/loading?)


(reg-sub
  ::deployment
  ::deployment-detail-spec/deployment)


(reg-sub
  ::events
  (fn [db]
    (::deployment-detail-spec/events db)))

(reg-sub
  ::global-deployment-parameters
  (fn [db]
    (::deployment-detail-spec/global-deployment-parameters db)))

(reg-sub
  ::node-parameters-modal
  (fn [db]
    (::deployment-detail-spec/node-parameters-modal db)))

(reg-sub
  ::node-parameters
  ::deployment-detail-spec/node-parameters)

(reg-sub
  ::summary-nodes-parameters
  (fn [db]
    (::deployment-detail-spec/summary-nodes-parameters db)))
