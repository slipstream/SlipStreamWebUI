(ns sixsq.slipstream.webui.deployment.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment.spec :as deployment-spec]))


(reg-sub
  ::loading?
  ::deployment-spec/loading?)


(reg-sub
  ::query-params
  ::deployment-spec/query-params)


(reg-sub
  ::deployments
  ::deployment-spec/deployments)


