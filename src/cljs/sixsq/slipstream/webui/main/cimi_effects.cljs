(ns sixsq.slipstream.webui.main.cimi-effects
  "Provides effects that use the CIMI client to interact asynchronously with
   the server."
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.api.authn :as authn]
    [sixsq.slipstream.webui.panel.authn.utils :as au]))


(reg-fx
  :fx.webui.main.cimi/cloud-entry-point
  (fn [[client callback]]
    (go
      (callback (<! (cimi/cloud-entry-point client))))))


(defn sanitize-params [params]
  (into {} (remove (comp nil? second) params)))


(reg-fx
  :fx.webui.main.cimi/search
  (fn [[client resource-type params callback]]
    (go
      (callback (<! (cimi/search client resource-type (sanitize-params params)))))))


(reg-fx
  :fx.webui.main.cimi/delete
  (fn [[client resource-id callback]]
    (go
      ;; FIXME: Using 2-arg form doesn't work with advanced optimization. Why?
      (callback (<! (cimi/delete client resource-id {}))))))


(reg-fx
  :fx.webui.main.cimi/edit
  (fn [[client resource-id data callback]]
    (go
      (callback (<! (cimi/edit client resource-id data))))))


(reg-fx
  :fx.webui.main.cimi/operation
  (fn [[client resource-id operation callback]]
    (go
      (callback (<! (cimi/operation client resource-id operation))))))


(reg-fx
  :fx.webui.main.cimi/logout
  (fn [[client callback]]
    (go
      (callback (<! (authn/logout client))))))


(reg-fx
  :fx.webui.main.cimi/login
  (fn [[client creds callback]]
    (go
      (let [resp (<! (authn/login client creds))
            session (<! (au/get-current-session client))]
        (callback resp session)))))


(reg-fx
  :fx.webui.main.cimi/session
  (fn [[client callback]]
    (go
      (callback (<! (au/get-current-session client))))))
