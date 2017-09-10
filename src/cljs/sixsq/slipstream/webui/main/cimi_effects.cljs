(ns sixsq.slipstream.webui.main.cimi-effects
  "Provides effects that use the CIMI client to interact asynchronously with
   the server."
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.panel.authn.utils :as au]))

(reg-fx
  :fx.webui.main.cimi/cloud-entry-point
  (fn [[client callback]]
    (go
      (callback (<! (cimi/cloud-entry-point client))))))

(reg-fx
  :fx.webui.main.cimi/search
  ;; FIXME: Parameters should not need to be filtered to work!
  (fn [[client resource-type params callback]]
    (go
      (callback (<! (cimi/search client resource-type (select-keys params #{:$first :$last :$filter})))))))

(reg-fx
  :fx.webui.main.cimi/delete
  (fn [[client resource-id callback]]
    (go
      (callback (<! (cimi/delete client resource-id))))))

(reg-fx
  :fx.webui.main.cimi/edit
  (fn [[client resource-id data callback]]
    (go
      (callback (<! (cimi/edit client resource-id data))))))

(reg-fx
  :fx.webui.main.cimi/logout
  (fn [[client callback]]
    (go
      (callback (<! (cimi/logout client))))))

(reg-fx
  :fx.webui.main.cimi/login
  (fn [[client creds callback]]
    (go
      (let [resp (<! (cimi/login client creds))
            session (<! (au/get-current-session client))]
        (callback resp session)))))

(reg-fx
  :fx.webui.main.cimi/session
  (fn [[client callback]]
    (go
      (callback (<! (au/get-current-session client))))))
