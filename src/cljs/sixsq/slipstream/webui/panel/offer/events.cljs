(ns sixsq.slipstream.webui.panel.offer.events
  (:require
    [clojure.set :as set]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.utils :as utils]
    [taoensso.timbre :as log]))

(reg-event-db
  :evt.webui.offer/set-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc-in db [:offer :cache :resource] data)))

(reg-event-db
  :evt.webui.offer/clear-data
  [db/debug-interceptors]
  (fn [db _]
    (assoc-in db [:offer :cache :resource] nil)))

(reg-event-db
  :evt.webui.offer/show-results
  [db/debug-interceptors trim-v]
  (fn [db [resource-type listing]]
    (let [entries (get listing (keyword resource-type) [])
          fields (utils/merge-keys (conj entries {:id "id"}))]
      (-> db
          (assoc-in [:offer :cache :resources] listing)
          (assoc-in [:offer :completed?] true)
          (assoc-in [:offer :fields :available] fields)))))

(reg-event-db
  :evt.webui.offer/set-param-first
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 1)]
      (assoc-in db [:offer :query-params :$first] n))))

(reg-event-db
  :evt.webui.offer/set-param-last
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (assoc-in db [:offer :query-params :$last] n))))

(reg-event-db
  :evt.webui.offer/set-param-filter
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (assoc-in db [:offer :query-params :$filter] v)))

(reg-event-db
  :evt.webui.offer/set-selected-fields
  [db/debug-interceptors trim-v]
  (fn [db [fields]]
    (let [fields (-> fields
                     (conj "id")
                     vec
                     distinct
                     vec)]
      (assoc-in db [:offer :fields :selected] fields))))

(reg-event-db
  :evt.webui.offer/remove-selected-field
  [db/debug-interceptors trim-v]
  (fn [db [field]]
    (let [current-fields (-> db :offer :fields :selected)
          fields (vec (remove #(= % field) current-fields))]
      (assoc-in db [:offer :fields :selected] fields))))

(reg-event-fx
  :offer
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{{:keys [clients offer cloud-entry-point]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name query-params]} offer
          {:keys [collection-key]} cloud-entry-point]
      (-> cofx
          (assoc-in [:db :offer :completed?] false)
          (assoc :fx.webui.offer/list [cimi-client (collection-key collection-name) (utils/prepare-params query-params)])))))

;; usage:  (dispatch [:set-offer-search [params]])
(reg-event-fx
  :set-offer
  [db/debug-interceptors trim-v]
  (fn [cofx [url-params]]
    (let [{{:keys [clients offer cloud-entry-point]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name query-params]} offer
          {:keys [collection-key]} cloud-entry-point
          new-params (utils/merge-offer-params query-params url-params)]
      (-> cofx
          (assoc-in [:db :offer :query-params] new-params)
          (assoc-in [:db :resource-path] ["offer"])
          (assoc :fx.webui.offer/list [cimi-client (collection-key collection-name) (utils/prepare-params new-params)])))))

(reg-event-fx
  :set-offer-detail
  [db/debug-interceptors trim-v]
  (fn [cofx [uuid]]
    (let [{{:keys [clients offer]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name]} offer]
      (-> cofx
          (assoc-in [:db :resource-path] ["offer" uuid])
          (assoc :fx.webui.offer/detail [cimi-client collection-name uuid])))))

(reg-event-fx
  :show-offer-table
  [db/debug-interceptors]
  (fn [cofx []]
    (-> cofx
        (assoc-in [:db :offer :cache :resources] nil)
        (assoc :fx.webui.history/navigate ["offer"]))))
