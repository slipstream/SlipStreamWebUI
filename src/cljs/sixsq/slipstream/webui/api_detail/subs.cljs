(ns sixsq.slipstream.webui.api-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.api-detail.spec :as api-detail-spec]))


(reg-sub
  ::loading?
  ::api-detail-spec/loading?)


(reg-sub
  ::resource-id
  ::api-detail-spec/resource-id)


(reg-sub
  ::resource
  ::api-detail-spec/resource)


(reg-sub
  ::description
  ::api-detail-spec/description)
