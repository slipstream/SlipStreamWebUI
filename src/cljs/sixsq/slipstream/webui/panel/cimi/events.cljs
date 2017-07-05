(ns sixsq.slipstream.webui.panel.cimi.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.utils :as utils]
    [clojure.set :as set]))

;; usage: (dispatch [:set-resource-data data])
(reg-event-db
  :set-resource-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc db :resource-data data)))

;; usage: (dispatch [:clear-resource-data data])
(reg-event-db
  :clear-resource-data
  [db/debug-interceptors]
  (fn [db _]
    (assoc db :resource-data nil)))

;; usage:  (dispatch [:cloud-entry-point])
;; triggers a fetch of the cloud entry point resource
(reg-event-fx
  :fetch-cloud-entry-point
  [db/debug-interceptors]
  (fn [cofx _]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.cimi/cloud-entry-point [client])
      cofx)))

;; usage:  (dispatch [:cloud-entry-point])
;; triggers a fetch of the cloud entry point resource
(reg-event-db
  :insert-cloud-entry-point
  [db/debug-interceptors trim-v]
  (fn [db [cep]]
    (assoc db :cloud-entry-point cep)))

;; usage:  (dispatch [:show-search-results results])
;; shows the search results
(reg-event-db
  :show-search-results
  [db/debug-interceptors trim-v]
  (fn [db [resource-type listing]]
    (let [entries (get listing (keyword resource-type) [])
          fields (utils/merge-keys (conj entries {:id "id"}))]
      (-> db
          (update-in [:search :listing] (constantly listing))
          (update-in [:search :completed?] (constantly true))
          (update-in [:search :available-fields] (constantly fields))))))

;; usage:  (dispatch [:set-search-first f])
(reg-event-db
  :set-search-first
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 1)]
      (update-in db [:search :params :$first] (constantly n)))))

;; usage:  (dispatch [:set-search-last f])
(reg-event-db
  :set-search-last
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (update-in db [:search :params :$last] (constantly n)))))

;; usage:  (dispatch [:set-search-filter f])
(reg-event-db
  :set-search-filter
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (update-in db [:search :params :$filter] (constantly v))))

;; usage:  (dispatch [:set-selected-fields fields])
(reg-event-db
  :set-selected-fields
  [db/debug-interceptors trim-v]
  (fn [db [fields]]
    (update-in db [:search :selected-fields] (constantly (set/union #{"id"} fields)))))

;; usage:  (dispatch [:remove-selected-field field])
(reg-event-db
  :remove-selected-field
  [db/debug-interceptors trim-v]
  (fn [db [field]]
    (update-in db [:search :selected-fields] #(set/difference % #{field}))))

;; usage:  (dispatch [:switch-search-resource resource-type])
;; trigger search on new resource type
(reg-event-fx
  :new-search
  [db/debug-interceptors trim-v]
  (fn [cofx [new-collection-name]]
    (let [cofx (assoc-in cofx [:db :search :collection-name] new-collection-name)
          {:keys [clients search]} (:db cofx)
          cimi-client (:cimi clients)
          {:keys [collection-name params]} search]
      (-> cofx
          (update-in [:db :search :completed?] (constantly false))
          (assoc :fx.webui.cimi/search [cimi-client collection-name (utils/prepare-params params)])))))

(reg-event-db
  :set-collection-name
  [db/debug-interceptors trim-v]
  (fn [db [new-collection-name]]
    (assoc-in db [:search :collection-name] new-collection-name)))

;; usage:  (dispatch [:search])
;; refine search
(reg-event-fx
  :search
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{:keys [clients search]} (:db cofx)
          cimi-client (:cimi clients)
          {:keys [collection-name params]} search]
      (-> cofx
          (update-in [:db :search :completed?] (constantly false))
          (assoc :fx.webui.cimi/search [cimi-client collection-name (utils/prepare-params params)])))))

