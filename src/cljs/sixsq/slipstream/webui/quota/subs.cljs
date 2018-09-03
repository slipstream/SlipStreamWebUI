(ns sixsq.slipstream.webui.quota.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.quota.spec :as quota-spec]))


(reg-sub
  ::loading?
  ::quota-spec/loading?)

(reg-sub
  ::credentials-quotas-map
  ::quota-spec/credentials-quotas-map)

