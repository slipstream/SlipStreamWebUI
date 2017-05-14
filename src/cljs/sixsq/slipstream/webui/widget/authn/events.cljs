(ns sixsq.slipstream.webui.widget.authn.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.widget.authn.utils :as au]))

;; usage: (dispatch [:evt.webui.authn/logout])
;; triggers logout through cimi client
(reg-event-fx
  :evt.webui.authn/logout
  [db/check-spec-interceptor]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.authn/logout [client])
      cofx)))

;; usage: (dispatch [:evt.webui.authn/logged-out])
;; updates app-db after successful logout
(reg-event-db
  :evt.webui.authn/logged-out
  [db/check-spec-interceptor]
  (fn [db _]
    (-> db
        (assoc-in [:authn :session] nil)
        (assoc-in [:authn :show-dialog?] false))))

;; usage: (dispatch [:evt.webui.authn/login creds])
;; triggers login through the cimi client
(reg-event-fx
  :evt.webui.authn/login
  [db/check-spec-interceptor trim-v]
  (fn [{:keys [db] :as cofx} [_]]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (let [method (-> db :authn :method)
            form-data (-> (get-in db [:authn :forms method])
                          (assoc :href method))
            cleared-form-data (au/clear-form-data (-> db :authn :forms))]
        (assoc cofx :fx.webui.authn/login [client form-data]))
      cofx)))

;; usage: (dispatch [:evt.webui.authn/logged-in session])
;; updates app-db after successful login
(reg-event-db
  :evt.webui.authn/logged-in
  [db/check-spec-interceptor trim-v]
  (fn [db [session]]
    (-> db
        (assoc-in [:authn :session] session)
        (assoc-in [:authn :show-dialog?] false))))

;;
;; Set the value to either show or hide the login dialog.
;;
;; [:evt.webui.authn/show-dialog show?]
;;
(reg-event-db
  :evt.webui.authn/show-dialog
  [db/check-spec-interceptor trim-v]
  (fn [db [show?]]
    (update-in db [:authn :show-dialog?] (constantly show?))))

;;
;; Starts the process to download the session templates from the server.
;; Should be dispatched once after the CIMI client has been initialized.
;;
;; [:evt.webui.authn/initialize]
;;
(reg-event-fx
  :evt.webui.authn/initialize
  [db/check-spec-interceptor]
  (fn [cofx [_]]
    (let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.authn/initialize [client]))))

;; usage: (dispatch [:check-session])
;; checks current session, marks user logged in/out as appropriate
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
;; [:evt.webui.authn/process-template tpl]
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
;; [:evt.webui.authn/add-template tpl]
;;
(reg-event-db
  :evt.webui.authn/add-template
  [db/check-spec-interceptor trim-v]
  (fn [db [{:keys [id] :as tpl}]]
    (let [form (au/login-form-fields tpl)]
      (-> db
          (update-in [:authn :methods] conj tpl)
          (update-in [:authn :forms] assoc id form)))))

;; usage: (dispatch [:evt.webui.authn/update-method method-id])
;; updates the currently selected login method
(reg-event-db
  :evt.webui.authn/update-method
  [db/check-spec-interceptor trim-v]
  (fn [db [method]]
    (assoc-in db [:authn :method] method)))

;; usage: (dispatch [:evt.webui.authn/update-form-data method-id param-name value])
;; updates the field in the form data with the given value
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
