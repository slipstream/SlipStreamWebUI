(ns sixsq.slipstream.webui.authn.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.history.effects :as history-fx]
    [sixsq.slipstream.webui.authn.spec :as authn-spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.i18n.utils :as utils]
    [taoensso.timbre :as log]))


(reg-event-fx
  ::initialize
  (fn [{:keys [db]} _]
    (when-let [client (-> db ::client-spec/client)]
      {::cimi-api-fx/session [client #(dispatch [::set-session %])]})))


(reg-event-fx
  ::set-session
  (fn [{:keys [db]} [_ session]]
    (let [redirect-uri (-> db ::authn-spec/redirect-uri)]
      (cond-> {:db (assoc db ::authn-spec/session session)}
              (and session redirect-uri)
              (assoc ::history-fx/navigate-js-location [redirect-uri])))))


(reg-event-fx
  ::logout
  (fn [cofx _]
    (when-let [client (-> cofx :db ::client-spec/client)]
      {::cimi-api-fx/logout [client #(dispatch [::set-session nil])]})))


(reg-event-db
  ::open-modal
  (fn [db [_ modal-key]]
    (assoc db ::authn-spec/open-modal modal-key)))


(reg-event-db
  ::close-modal
  (fn [db _]
    (assoc db ::authn-spec/open-modal nil)))


(reg-event-db
  ::set-error-message
  (fn [db [_ error-message]]
    (assoc db ::authn-spec/error-message error-message)))


(reg-event-db
  ::clear-error-message
  (fn [db _]
    (assoc db ::authn-spec/error-message nil)))


(reg-event-db
  ::redirect-uri
  (fn [db [_ uri]]
    (assoc db ::authn-spec/redirect-uri uri)))


(reg-event-db
  ::server-redirect-uri
  (fn [db [_ uri]]
    (assoc db ::authn-spec/server-redirect-uri uri)))
