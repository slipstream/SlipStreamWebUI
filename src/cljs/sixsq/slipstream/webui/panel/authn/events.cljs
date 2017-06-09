(ns sixsq.slipstream.webui.panel.authn.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.panel.authn.utils :as au]
    [sixsq.slipstream.webui.utils :as utils]
    [taoensso.timbre :as log]))

(reg-event-fx
  :evt.webui.authn/logout
  [db/check-spec-interceptor]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.authn/logout [client])
      cofx)))

(reg-event-db
  :evt.webui.authn/logged-out
  [db/check-spec-interceptor]
  (fn [db _]
    (-> db
        (assoc-in [:authn :session] nil))))

(reg-event-fx
  :evt.webui.authn/login
  [db/check-spec-interceptor trim-v]
  (fn [{:keys [db] :as cofx} _]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (let [method (-> db :authn :method)
            form-data (-> (get-in db [:authn :forms method])
                          (assoc :href method))
            cleared-form-data (au/clear-form-data (-> db :authn :forms))]
        (-> cofx
            (assoc-in [:db :authn :forms] cleared-form-data)
            (assoc :fx.webui.authn/login [client form-data])))
      cofx)))

(reg-event-db
  :evt.webui.authn/logged-in
  [db/check-spec-interceptor trim-v]
  (fn [db [session]]
    (-> db
        (assoc-in [:authn :session] session))))

(reg-event-fx
  :evt.webui.authn/initialize
  [db/check-spec-interceptor]
  (fn [cofx [_]]
    (let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.authn/initialize [client]))))

;;
;; checks current session, marks user logged in/out as appropriate
;; used during initialization
;;
(reg-event-fx
  :evt.webui.authn/check-session
  [db/check-spec-interceptor]
  (fn [cofx [_]]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.authn/check-session [client])
      cofx)))

;;
;; For a given (minimized) session template, starts the process to
;; download the parameter description.
;;
(reg-event-fx
  :evt.webui.authn/process-template
  [db/check-spec-interceptor trim-v]
  (fn [cofx [tpl]]
    (assoc cofx :fx.webui.authn/process-template [tpl])))

;;
;; For a complete session template, add the information to the database
;; to drive the login widgets.
;;
(reg-event-db
  :evt.webui.authn/add-template
  [db/check-spec-interceptor trim-v]
  (fn [db [{:keys [id] :as tpl}]]
    (let [form (au/login-form-fields tpl)]
      (-> db
          (update-in [:authn :methods] conj tpl)
          (update-in [:authn :forms] assoc id form)))))

(reg-event-db
  :evt.webui.authn/update-method
  [db/check-spec-interceptor trim-v]
  (fn [db [method]]
    (assoc-in db [:authn :method] method)))

(reg-event-db
  :evt.webui.authn/update-form-data
  [db/check-spec-interceptor trim-v]
  (fn [db [[method param-name value]]]
    (assoc-in db [:authn :forms method param-name] value)))

(reg-event-db
  :evt.webui.authn/clear-form-data
  [db/check-spec-interceptor trim-v]
  (fn [db [_]]
    (let [cleared-form-data (au/clear-form-data (-> db :authn :forms))]
      (assoc-in db [:authn :forms] cleared-form-data))))

(reg-event-db
  :set-login-path-and-error
  [db/check-spec-interceptor trim-v]
  (fn [db [error-message]]
    (-> db
        (assoc :resource-path (utils/parse-resource-path "/login"))
        (assoc-in [:authn :error-message] error-message))))

(reg-event-db
  :evt.webui.authn/clear-error-message
  [db/check-spec-interceptor trim-v]
  (fn [db [_]]
    (assoc-in db [:authn :error-message] nil)))

