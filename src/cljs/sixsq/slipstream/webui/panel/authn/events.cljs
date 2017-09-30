(ns sixsq.slipstream.webui.panel.authn.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v dispatch]]
    [sixsq.slipstream.webui.panel.authn.utils :as au]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.main.cimi-effects :as cimi-effects]
    [taoensso.timbre :as log]))

(reg-event-fx
  :evt.webui.authn/logout
  [db/debug-interceptors]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.main.cimi/logout
                  [client (fn [resp]
                            (if (= 200 (:status resp))
                              (dispatch [:evt.webui.authn/logged-out])
                              (dispatch [:message "logout failed"])))])
      cofx)))

(reg-event-db
  :evt.webui.authn/logged-out
  [db/debug-interceptors]
  (fn [db _]
    (-> db
        (assoc-in [:authn :session] nil))))

(reg-event-db
  :evt.webui.authn/set-redirect-uri
  [db/debug-interceptors trim-v]
  (fn [db [redirect-uri]]
    (-> db
        (assoc-in [:authn :redirect-uri] redirect-uri))))

(reg-event-fx
  :evt.webui.authn/login
  [db/debug-interceptors trim-v]
  (fn [{:keys [db] :as cofx} _]
    (if-let [client (-> cofx :db :clients :cimi)]
      (let [method (-> db :authn :method)
            form-data (-> (get-in db [:authn :forms method])
                          (assoc :href method))
            cleared-form-data (au/clear-form-data (-> db :authn :forms))]
        (-> cofx
            (assoc-in [:db :authn :forms] cleared-form-data)
            (assoc :fx.webui.main.cimi/login
                   [client
                    form-data
                    (fn [resp session]
                      (if session
                        (dispatch [:evt.webui.authn/logged-in session])
                        (do
                          (log/error "Error login response:" (with-out-str (cljs.pprint/pprint resp)))
                          (dispatch [:message "login failed"]))))])))
      cofx)))

(reg-event-db
  :evt.webui.authn/logged-in
  [db/debug-interceptors trim-v]
  (fn [db [session]]
    (-> db
        (assoc-in [:authn :session] session))))

(reg-event-fx
  :evt.webui.authn/initialize
  [db/debug-interceptors]
  (fn [cofx [_]]
    (let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.authn/initialize [client]))))

(reg-event-db
  :evt.webui.authn/show-modal
  [db/debug-interceptors]
  (fn [db _]
    (assoc-in db [:authn :show-modal?] true)))

(reg-event-db
  :evt.webui.authn/hide-modal
  [db/debug-interceptors]
  (fn [db _]
    (assoc-in db [:authn :show-modal?] false)))

;;
;; checks current session, marks user logged in/out as appropriate
;; used during initialization
;;
(reg-event-fx
  :evt.webui.authn/check-session
  [db/debug-interceptors]
  (fn [cofx [_]]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.main.cimi/session
                  [client
                   (fn [session]
                     (if session
                       (dispatch [:evt.webui.authn/logged-in session])
                       (dispatch [:evt.webui.authn/logged-out])))])
      cofx)))

;;
;; For a given (minimized) session template, starts the process to
;; download the parameter description.
;;
(reg-event-fx
  :evt.webui.authn/process-template
  [db/debug-interceptors trim-v]
  (fn [cofx [tpl]]
    (assoc cofx :fx.webui.authn/process-template [tpl])))

;;
;; For a complete session template, add the information to the database
;; to drive the login widgets.
;;
(reg-event-db
  :evt.webui.authn/add-template
  [db/debug-interceptors trim-v]
  (fn [db [{:keys [id] :as tpl}]]
    (let [form (au/login-form-fields tpl)]
      (-> db
          (update-in [:authn :count] inc)
          (update-in [:authn :methods] conj tpl)
          (update-in [:authn :forms] assoc id form)))))

(reg-event-db
  :evt.webui.authn/set-methods-total
  [db/debug-interceptors trim-v]
  (fn [db [n]]
    (-> db
        (assoc-in [:authn :total] n)
        (assoc-in [:authn :count] 0))))

(reg-event-db
  :evt.webui.authn/update-method
  [db/debug-interceptors trim-v]
  (fn [db [method]]
    (assoc-in db [:authn :method] method)))

(reg-event-db
  :evt.webui.authn/update-form-data
  [db/debug-interceptors trim-v]
  (fn [db [[method param-name value]]]
    (assoc-in db [:authn :forms method param-name] value)))

(reg-event-db
  :evt.webui.authn/clear-form-data
  [db/debug-interceptors trim-v]
  (fn [db [_]]
    (let [cleared-form-data (au/clear-form-data (-> db :authn :forms))]
      (assoc-in db [:authn :forms] cleared-form-data))))

(reg-event-db
  :set-login-path-and-error
  [db/debug-interceptors trim-v]
  (fn [db [error-message]]
    (-> db
        (assoc :resource-path (utils/parse-resource-path "/login"))
        (assoc-in [:authn :error-message] error-message))))

(reg-event-db
  :evt.webui.authn/set-error-message
  [db/debug-interceptors trim-v]
  (fn [db [error-message]]
    (assoc-in db [:authn :error-message] error-message)))

(reg-event-db
  :evt.webui.authn/clear-error-message
  [db/debug-interceptors trim-v]
  (fn [db [_]]
    (assoc-in db [:authn :error-message] nil)))

(reg-event-db
  :evt.webui.authn/no-modal-login
  [db/debug-interceptors trim-v]
  (fn [db [_]]
    (assoc-in db [:authn :use-modal?] false)))

(reg-event-db
  :evt.webui.authn/show-modal
  [db/debug-interceptors trim-v]
  (fn [db [_]]
    (assoc-in db [:authn :show-modal?] true)))

(reg-event-db
  :evt.webui.authn/hide-modal
  [db/debug-interceptors trim-v]
  (fn [db [_]]
    (assoc-in db [:authn :show-modal?] false)))

