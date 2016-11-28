(ns sixsq.slipstream.webui.events
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v after]]
    [cljs.spec :as s]
    [sixsq.slipstream.client.api.cimi.async :as cimi-async]))

;;
;; check schema after every change
;;
(defn check-and-throw
  "throw an exception if db doesn't match the spec."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; check that all state changes from event handlers are correct
(def check-spec-interceptor (after (partial check-and-throw :sixsq.slipstream.webui.db/db)))

;;
;; event handlers
;;

;; usage:  (dispatch [:initialize-db])
;; creates initial state of database
(reg-event-fx
  :initialize-db
  [check-spec-interceptor]
  (fn [_ _]
    {:db db/default-value}))

;; usage:  (dispatch [:initialize-client])
;; creates and adds a SlipStream client to the database
(reg-event-db
  :initialize-client
  [check-spec-interceptor]
  (fn [db _]
    (assoc db :client (cimi-async/instance))))

;; usage: (dispatch [:logout])
;; triggers logout through cimi client
(reg-event-fx
  :logout
  [check-spec-interceptor]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :client])]
      (assoc cofx :cimi/logout [client])
      cofx)))

;; usage: (dispatch [:logged-out])
;; updates app-db after successful logout
(reg-event-db
  :logged-out
  [check-spec-interceptor]
  (fn [db _]
    (assoc db :authn {:logged-in? false :user-id nil})))

;; usage: (dispatch [:login creds])
;; triggers login through the cimi client
(reg-event-fx
  :login
  [check-spec-interceptor trim-v]
  (fn [cofx [creds]]
    (if-let [client (get-in cofx [:db :client])]
      (assoc cofx :cimi/login [client creds])
      cofx)))

;; usage: (dispatch [:logged-in user-id])
;; updates app-db after successful login
(reg-event-db
  :logged-in
  [check-spec-interceptor trim-v]
  (fn [db [user-id]]
    (assoc db :authn {:logged-in? true :user-id user-id})))

;; usage: (dispatch [:message msg])
;; displays a message
(reg-event-fx
  :message
  [check-spec-interceptor trim-v]
  (fn [{:keys [db]} [msg]]
    {:db             (assoc db :message msg)
     :dispatch-later [{:ms 1000 :dispatch [:message nil]}]}))

;; usage:  (dispatch [:cloud-entry-point])
;; triggers a fetch of the cloud entry point resource
(reg-event-fx
  :fetch-cloud-entry-point
  [check-spec-interceptor]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :client])]
      (assoc cofx :cimi/cloud-entry-point [client])
      cofx)))

;; usage:  (dispatch [:cloud-entry-point])
;; triggers a fetch of the cloud entry point resource
(reg-event-db
  :insert-cloud-entry-point
  [check-spec-interceptor trim-v]
  (fn [db [cep]]
    (assoc db :cloud-entry-point cep)))

;; usage:  (dispatch [:switch-search-resource resource-type])
;; trigger search on new resource type
(reg-event-fx
  :switch-search-resource
  [check-spec-interceptor trim-v]
  (fn [cofx [resource-type]]
    (if-let [client (get-in cofx [:db :client])]
      (assoc cofx :cimi/search [client resource-type])
      cofx)))

;; usage:  (dispatch [:show-search-results results])
;; shows the search results
(reg-event-db
  :show-search-results
  [check-spec-interceptor trim-v]
  (fn [db [results]]
    (assoc db :results (str results))))

