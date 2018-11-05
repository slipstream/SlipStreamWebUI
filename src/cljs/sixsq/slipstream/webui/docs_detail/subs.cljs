(ns sixsq.slipstream.webui.docs-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment-detail.spec :as deployment-detail-spec]))


(reg-sub
  ::runUUID
  ::deployment-detail-spec/runUUID)


(reg-sub
  ::reports
  ::deployment-detail-spec/reports)


(reg-sub
  ::loading?
  ::deployment-detail-spec/loading?)


(reg-sub
  ::cached-resource-id
  ::deployment-detail-spec/cached-resource-id)


(reg-sub
  ::resource
  ::deployment-detail-spec/resource)


(reg-sub
  ::events
  ::deployment-detail-spec/events)
