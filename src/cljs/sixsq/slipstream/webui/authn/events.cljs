(ns sixsq.slipstream.webui.authn.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]))

;; usage: (dispatch [:logout])
;; triggers logout through cimi client
(reg-event-fx
  :logout
  [db/check-spec-interceptor]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :client])]
      (assoc cofx :cimi/logout [client])
      cofx)))

;; usage: (dispatch [:logged-out])
;; updates app-db after successful logout
(reg-event-db
  :logged-out
  [db/check-spec-interceptor]
  (fn [db _]
    (assoc db :authn {:logged-in? false :user-id nil})))

;; usage: (dispatch [:login creds])
;; triggers login through the cimi client
(reg-event-fx
  :login
  [db/check-spec-interceptor trim-v]
  (fn [cofx [creds]]
    (if-let [client (get-in cofx [:db :client])]
      (assoc cofx :cimi/login [client creds])
      cofx)))

;; usage: (dispatch [:logged-in user-id])
;; updates app-db after successful login
(reg-event-db
  :logged-in
  [db/check-spec-interceptor trim-v]
  (fn [db [user-id]]
    (assoc db :authn {:logged-in? true :user-id user-id})))
