(ns sixsq.slipstream.webui.authn.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.authn.effects :as authn-fx]
    [sixsq.slipstream.webui.history.effects :as history-fx]
    [sixsq.slipstream.webui.authn.spec :as authn-spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.i18n.utils :as utils]
    [taoensso.timbre :as log]))

(reg-event-db
  ::set-methods-total
  (fn [db [_ n]]
    (-> db
        (assoc-in [::authn-spec/total] n)
        (assoc-in [::authn-spec/count] 0))))


(reg-event-db
  ::add-template
  (fn [db [_ {:keys [id] :as tpl}]]
    (-> db
        (update-in [::authn-spec/count] inc)
        (update-in [::authn-spec/methods] conj tpl))))


(reg-event-fx
  ::process-template
  (fn [cofx [_ tpl]]
    {::authn-fx/process-template [tpl #(dispatch [::add-template %])]}))


(reg-event-fx
  ::initialize
  (fn [{:keys [db]} _]
    (when-let [client (-> db ::client-spec/client)]
      {::cimi-api-fx/session [client #(dispatch [::set-session %])]
       ::authn-fx/initialize [client
                              (fn [tpls]
                                (dispatch [::set-methods-total (count tpls)])
                                (doseq [tpl tpls]
                                  (dispatch [::process-template tpl])))]})))


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
  (fn [db _]
    (assoc db ::authn-spec/modal-open? true)))


(reg-event-db
  ::close-modal
  (fn [db _]
    (assoc db ::authn-spec/modal-open? false)))


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
