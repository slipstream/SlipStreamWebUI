(ns sixsq.slipstream.webui.main.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.client.api.cimi.async :as cimi-async]
    [sixsq.slipstream.client.api.runs.async :as runs-async]
    [sixsq.slipstream.client.api.modules.async :as modules-async]
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.main.effects :as effects]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.panel.cimi.utils :as u]
    [taoensso.timbre :as log]))

(reg-event-fx
  :evt.webui.main/initialize-db
  [db/debug-interceptors]
  (fn [_ _]
    {:db db/default-value}))

(reg-event-db
  :evt.webui.main/initialize-client
  [db/debug-interceptors trim-v]
  (fn [db [slipstream-url]]
    (let [clients {:cimi    (cimi-async/instance (str slipstream-url "/api/cloud-entry-point"))
                   :runs    (runs-async/instance (str slipstream-url "/run")
                                                 (str slipstream-url "/auth/login")
                                                 (str slipstream-url "/auth/logout"))
                   :modules (modules-async/instance (str slipstream-url "/module")
                                                    (str slipstream-url "/auth/login")
                                                    (str slipstream-url "/auth/logout"))}]
      (assoc db :clients clients))))

(reg-event-fx
  :evt.webui.main/load-cloud-entry-point
  [db/debug-interceptors]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.main/cloud-entry-point [client])
      cofx)))

(reg-event-db
  :evt.webui.main/set-cloud-entry-point
  [db/debug-interceptors trim-v]
  (fn [db [{:keys [baseURI] :as cep}]]
    (let [href-map (u/collection-href-map cep)
          key-map (u/collection-key-map cep)]
      (assoc db :cloud-entry-point {:baseURI         baseURI
                                    :collection-href href-map
                                    :collection-key  key-map}))))

;; usage: (dispatch [:message msg])
;; displays a message
(reg-event-fx
  :message
  [db/debug-interceptors trim-v]
  (fn [{:keys [db]} [msg]]
    {:db             (assoc db :message msg)
     :dispatch-later [{:ms 3000 :dispatch [:clear-message]}]}))

;; usage: (dispatch [:clear-message])
;; clears a message
(reg-event-db
  :clear-message
  [db/debug-interceptors]
  (fn [db _]
    (assoc db :message nil)))

;; usage:  (dispatch [:set-resource-path path])
(reg-event-db
  :set-resource-path
  [db/debug-interceptors trim-v]
  (fn [db [path]]
    (assoc db :resource-path (utils/parse-resource-path path))))

(reg-event-db
  :set-resource-path-vec
  [db/debug-interceptors trim-v]
  (fn [db [path]]
    (assoc db :resource-path path)))

(reg-event-db
  :set-resource
  [db/debug-interceptors trim-v]
  (fn [db [path query-params]]
    (-> db
        (assoc :resource-path path)
        (assoc :resource-query-params query-params))))

(reg-event-fx
  :evt.webui.main/set-host-theme
  [db/debug-interceptors trim-v]
  (fn [cofx [path-prefix]]
    (assoc cofx :fx.webui.main/set-host-theme [])))

(reg-event-db
  :evt.webui.main/clear-alert
  [db/debug-interceptors]
  (fn [db _]
    (assoc db :alert nil)))

(reg-event-db
  :evt.webui.main/raise-alert
  [db/debug-interceptors trim-v]
  (fn [db [alert]]
    (assoc db :alert alert)))

