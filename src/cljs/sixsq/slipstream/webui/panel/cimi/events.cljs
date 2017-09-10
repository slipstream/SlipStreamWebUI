(ns sixsq.slipstream.webui.panel.cimi.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v dispatch]]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.panel.cimi.utils :as u]
    [sixsq.slipstream.webui.main.cimi-effects :as cimi-effects]
    [clojure.set :as set]
    [taoensso.timbre :as log]
    [clojure.string :as str]))

(reg-event-db
  :set-resource-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc db :resource-data data)))

(reg-event-db
  :clear-resource-data
  [db/debug-interceptors]
  (fn [db _]
    (assoc db :resource-data nil)))

(reg-event-db
  :show-search-results
  [db/debug-interceptors trim-v]
  (fn [db [resource-type listing]]
    (let [entries (get listing (keyword resource-type) [])
          aggregations (:aggregations listing)
          fields (utils/merge-keys (conj entries {:id "id"}))]
      (-> db
          (assoc-in [:search :cache :aggregations] aggregations)
          (assoc-in [:search :cache :resources] listing)
          (assoc-in [:search :completed?] true)
          (assoc-in [:search :fields :available] fields)))))

(reg-event-db
  :set-search-first
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 1)]
      (assoc-in db [:search :query-params :$first] n))))

(reg-event-db
  :set-search-last
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (assoc-in db [:search :query-params :$last] n))))

(reg-event-db
  :set-search-filter
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (assoc-in db [:search :query-params :$filter] v)))

(reg-event-db
  :evt.webui.cimi/set-orderby
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (assoc-in db [:search :query-params :$orderby] v)))

(reg-event-db
  :evt.webui.cimi/set-select
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (if (str/blank? v)
      (assoc-in db [:search :query-params :$select] nil)
      (let [v (str/join "," (->> (str/split v #"\s*,\s*")
                                 (map str/trim)
                                 set
                                 (cons "id")))]
        (assoc-in db [:search :query-params :$select] v)))))

(reg-event-db
  :evt.webui.cimi/set-aggregation
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (assoc-in db [:search :query-params :$aggregation] v)))

(reg-event-db
  :set-selected-fields
  [db/debug-interceptors trim-v]
  (fn [db [fields]]
    (let [fields (-> fields
                     (conj "id")
                     vec
                     distinct
                     vec)]
      (assoc-in db [:search :fields :selected] fields))))

(reg-event-db
  :remove-selected-field
  [db/debug-interceptors trim-v]
  (fn [db [field]]
    (let [current-fields (-> db :search :fields :selected)
          fields (vec (remove #(= % field) current-fields))]
      (assoc-in db [:search :fields :selected] fields))))

(reg-event-fx
  :new-search
  [db/debug-interceptors trim-v]
  (fn [cofx [new-collection-name]]
    (let [cofx (assoc-in cofx [:db :search :collection-name] new-collection-name)
          {:keys [clients search]} (:db cofx)
          cimi-client (:cimi clients)
          {:keys [collection-name params]} search]
      (-> cofx
          (assoc-in [:db :search :completed?] false)
          (assoc :fx.webui.main.cimi/search [cimi-client
                                             collection-name
                                             (utils/prepare-params params)
                                             #(dispatch [:show-search-results collection-name %])])))))

(reg-event-db
  :set-collection-name
  [db/debug-interceptors trim-v]
  (fn [db [new-collection-name]]
    (assoc-in db [:search :collection-name] new-collection-name)))

(reg-event-fx
  :search
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{:keys [clients search cloud-entry-point]} (:db cofx)
          cimi-client (:cimi clients)
          {:keys [collection-name query-params]} search
          {:keys [collection-key]} cloud-entry-point]
      (-> cofx
          (assoc-in [:db :search :completed?] false)
          (assoc :fx.webui.main.cimi/search [cimi-client
                                             (collection-key collection-name)
                                             (utils/prepare-params query-params)
                                             #(dispatch [:show-search-results (collection-key collection-name) %])])))))

(reg-event-fx
  :evt.webui.cimi/delete
  [db/debug-interceptors trim-v]
  (fn [cofx [resource-id]]
    (let [cimi-client (-> cofx :db :clients :cimi)]
      (assoc cofx :fx.webui.main.cimi/delete [cimi-client resource-id u/dispatch-alert]))))

(reg-event-fx
  :evt.webui.cimi/edit
  [db/debug-interceptors trim-v]
  (fn [cofx [resource-id data]]
    (if (u/dispatch-alert-on-error data)
      cofx
      (let [cimi-client (-> cofx :db :clients :cimi)]
        (assoc cofx :fx.webui.main.cimi/edit [cimi-client resource-id data u/dispatch-edit-alert])))))
