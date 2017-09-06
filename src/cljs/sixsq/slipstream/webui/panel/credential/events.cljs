(ns sixsq.slipstream.webui.panel.credential.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v dispatch]]
    [sixsq.slipstream.webui.panel.cimi.utils :as u]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.panel.credential.utils :as panel-utils]))

(reg-event-db
  :evt.webui.credential/set-description
  [db/debug-interceptors trim-v]
  (fn [db [description]]
    (let [description-map {(:id description) description}]
      (update-in db [:credentials :descriptions] merge description-map))))

(reg-event-fx
  :evt.webui.credential/get-description
  [db/debug-interceptors trim-v]
  (fn [cofx [template]]
    (assoc cofx :fx.webui.credential/get-description [template])))

(reg-event-fx
  :evt.webui.credential/get-templates
  [db/debug-interceptors]
  (fn [cofx _]
    (when-let [cimi-client (-> cofx :db :clients :cimi)]
      (assoc cofx :fx.webui.credential/get-templates [cimi-client]))))

(reg-event-db
  :evt.webui.credential/hide-modal
  [db/debug-interceptors]
  (fn [db _]
    (assoc-in db [:credentials :show-modal?] false)))

(reg-event-db
  :evt.webui.credential/show-modal
  [db/debug-interceptors]
  (fn [db _]
    (assoc-in db [:credentials :show-modal?] true)))

(reg-event-fx
  :evt.webui.credential/create-credential
  [db/debug-interceptors trim-v]
  (fn [cofx [form-data]]
    (when-let [cimi-client (-> cofx :db :clients :cimi)]
      (let [request-body (panel-utils/create-template form-data)]
        (assoc cofx :fx.webui.credential/create-credential [cimi-client request-body])))))
