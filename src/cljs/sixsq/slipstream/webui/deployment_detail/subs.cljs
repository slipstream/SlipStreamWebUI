(ns sixsq.slipstream.webui.deployment-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment-detail.spec :as deployment-detail-spec]))


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
  ::deployment-detail-spec/events)

(reg-sub
  ::global-deployment-parameters
  ::deployment-detail-spec/global-deployment-parameters)

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
