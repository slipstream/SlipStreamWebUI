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

(reg-sub
  ::elements-per-page
  ::deployment-spec/elements-per-page)


(reg-sub
  ::page
  ::deployment-spec/page)


(reg-sub
  ::deployments-creds-map
  ::deployment-spec/deployments-creds-map)
