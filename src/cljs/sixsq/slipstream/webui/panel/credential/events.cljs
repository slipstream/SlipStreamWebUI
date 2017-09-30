(ns sixsq.slipstream.webui.panel.credential.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v dispatch]]
    [sixsq.slipstream.webui.panel.cimi.utils :as u]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.panel.credential.utils :as panel-utils]
    [sixsq.slipstream.webui.utils :as utils]))

(reg-event-db
  :evt.webui.credential/set-description
  [db/debug-interceptors trim-v]
  (fn [db [description]]
    (let [description-map {(:id description) description}]
      (update-in db [:credential :descriptions] merge description-map))))

(reg-event-fx
  :evt.webui.credential/get-description
  [db/debug-interceptors trim-v]
  (fn [cofx [template]]
    (assoc cofx :fx.webui.credential/get-description [template])))

(reg-event-fx
  :evt.webui.credential/get-templates
  [db/debug-interceptors]
  (fn [cofx _]
    (when-let [cimi-client (-> cofx :db :clients :cimi)]
      (assoc cofx :fx.webui.credential/get-templates [cimi-client]))))

(reg-event-db
  :evt.webui.credential/hide-modal
  [db/debug-interceptors]
  (fn [db _]
    (assoc-in db [:credential :show-modal?] false)))

(reg-event-db
  :evt.webui.credential/show-modal
  [db/debug-interceptors]
  (fn [db _]
    (assoc-in db [:credential :show-modal?] true)))

(reg-event-fx
  :evt.webui.credential/create-credential
  [db/debug-interceptors trim-v]
  (fn [cofx [form-data]]
    (when-let [cimi-client (-> cofx :db :clients :cimi)]
      (let [request-body (panel-utils/create-template form-data)]
        (assoc cofx :fx.webui.credential/create-credential [cimi-client request-body])))))

(reg-event-db
  :evt.webui.credential/set-selected-fields
  [db/debug-interceptors trim-v]
  (fn [db [fields]]
    (let [fields (-> fields
                     (conj "id")
                     vec
                     distinct
                     vec)]
      (assoc-in db [:credential :fields :selected] fields))))

(reg-event-db
  :evt.webui.credential/remove-selected-field
  [db/debug-interceptors trim-v]
  (fn [db [field]]
    (let [current-fields (-> db :credential :fields :selected)
          fields (vec (remove #(= % field) current-fields))]
      (assoc-in db [:credential :fields :selected] fields))))

(reg-event-db
  :evt.webui.credential/set-first
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 1)]
      (assoc-in db [:credential :query-params :$first] n))))

(reg-event-db
  :evt.webui.credential/set-last
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (assoc-in db [:credential :query-params :$last] n))))

(reg-event-db
  :evt.webui.credential/set-filter
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (assoc-in db [:credential :query-params :$filter] v)))

(reg-event-db
  :evt.webui.credential/show-search-results
  [db/debug-interceptors trim-v]
  (fn [db [resource-type listing]]
    (let [entries (get listing (keyword resource-type) [])
          fields (utils/merge-keys (conj entries {:id "id"}))]
      (-> db
          (assoc-in [:credential :cache :resources] listing)
          (assoc-in [:credential :completed?] true)
          (assoc-in [:credential :fields :available] fields)))))

(reg-event-fx
  :evt.webui.credential/search
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{:keys [clients credential cloud-entry-point]} (:db cofx)
          cimi-client (:cimi clients)
          {:keys [collection-name query-params]} credential
          {:keys [collection-key]} cloud-entry-point]
      (-> cofx
          (assoc-in [:db :credential :completed?] false)
          (assoc :fx.webui.main.cimi/search [cimi-client
                                             (collection-key collection-name)
                                             (utils/prepare-params query-params)
                                             #(dispatch [:evt.webui.credential/show-search-results (collection-key collection-name) %])])))))

