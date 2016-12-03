(ns sixsq.slipstream.webui.events
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v after]]
    [re-frame.loggers :refer [console]]
    [cljs.spec :as s]
    [sixsq.slipstream.client.api.cimi.async :as cimi-async]
    [sixsq.slipstream.webui.utils :as utils]
    [clojure.set :as set]))

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
     :dispatch-later [{:ms 3000 :dispatch [:clear-message]}]}))

;; usage: (dispatch [:clear-message])
;; clears a message
(reg-event-db
  :clear-message
  [check-spec-interceptor]
  (fn [db _]
    (assoc db :message nil)))

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

;; usage:  (dispatch [:show-search-results results])
;; shows the search results
(reg-event-db
  :show-search-results
  [check-spec-interceptor trim-v]
  (fn [db [resource-type results]]
    (let [entries (get results (keyword resource-type) [])
          fields (utils/merge-keys (conj entries {:id "id"}))]
      (-> db
          (update-in [:search :results] (constantly results))
          (update-in [:search :completed?] (constantly true))
          (update-in [:search :available-fields] (constantly fields))))))

;; usage:  (dispatch [:set-search-first f])
(reg-event-db
  :set-search-first
  [check-spec-interceptor trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 1)]
      (update-in db [:search :params :$first] (constantly n)))))

;; usage:  (dispatch [:set-search-last f])
(reg-event-db
  :set-search-last
  [check-spec-interceptor trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (update-in db [:search :params :$last] (constantly n)))))

;; usage:  (dispatch [:set-search-filter f])
(reg-event-db
  :set-search-filter
  [check-spec-interceptor trim-v]
  (fn [db [v]]
    (update-in db [:search :params :$filter] (constantly v))))

;; usage:  (dispatch [:set-selected-fields fields])
(reg-event-db
  :set-selected-fields
  [check-spec-interceptor trim-v]
  (fn [db [fields]]
    (update-in db [:search :selected-fields] (constantly (set/union #{"id"} fields)))))

;; usage:  (dispatch [:switch-search-resource resource-type])
;; trigger search on new resource type
(reg-event-fx
  :new-search
  [check-spec-interceptor trim-v]
  (fn [cofx [new-collection-name]]
    (let [cofx (assoc-in cofx [:db :search :collection-name] new-collection-name)
          {:keys [client search]} (:db cofx)
          {:keys [collection-name params]} search]
      (-> cofx
          (update-in [:db :search :completed?] (constantly false))
          (assoc :cimi/search [client collection-name (utils/prepare-params params)])))))

;; usage:  (dispatch [:search])
;; refine search
(reg-event-fx
  :search
  [check-spec-interceptor]
  (fn [cofx _]
    (let [{:keys [client search]} (:db cofx)
          {:keys [collection-name params]} search]
      (-> cofx
          (update-in [:db :search :completed?] (constantly false))
          (assoc :cimi/search [client collection-name (utils/prepare-params params)])))))
