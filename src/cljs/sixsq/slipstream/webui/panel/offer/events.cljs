(ns sixsq.slipstream.webui.panel.offer.events
  (:require
    [clojure.set :as set]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.utils :as utils]))

;; usage: (dispatch [:set-offer-data data])
(reg-event-db
  :evt.webui.offer/set-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc db :offer-data data)))

;; usage: (dispatch [:clear-offer-data data])
(reg-event-db
  :evt.webui.offer/clear-data
  [db/debug-interceptors]
  (fn [db _]
    (assoc db :offer-data nil)))

;; usage:  (dispatch [:show-offer-results results])
;; shows the offer results
(reg-event-db
  :evt.webui.offer/show-results
  [db/debug-interceptors trim-v]
  (fn [db [resource-type listing]]
    (let [entries (get listing (keyword resource-type) [])
          fields (utils/merge-keys (conj entries {:id "id"}))]
      (-> db
          (update-in [:offer :listing] (constantly listing))
          (update-in [:offer :completed?] (constantly true))
          (update-in [:offer :available-fields] (constantly fields))))))

;; usage:  (dispatch [:set-offer-first f])
(reg-event-db
  :evt.webui.offer/set-param-first
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 1)]
      (assoc-in db [:offer :params :$first] n))))

;; usage:  (dispatch [:set-offer-last f])
(reg-event-db
  :evt.webui.offer/set-param-last
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (assoc-in db [:offer :params :$last] n))))

;; usage:  (dispatch [:set-offer-filter f])
(reg-event-db
  :evt.webui.offer/set-param-filter
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (assoc-in db [:offer :params :$filter] v)))

;; usage:  (dispatch [:set-selected-fields fields])
(reg-event-db
  :evt.webui.offer/set-selected-fields
  [db/debug-interceptors trim-v]
  (fn [db [fields]]
    (assoc-in db [:offer :selected-fields] (set/union #{"id"} fields))))

;; usage:  (dispatch [:remove-selected-field field])
(reg-event-db
  :evt.webui.offer/remove-selected-field
  [db/debug-interceptors trim-v]
  (fn [db [field]]
    (update-in db [:offer :selected-fields] #(set/difference % #{field}))))

;; usage:  (dispatch [:offer])
;; refine offer
(reg-event-fx
  :offer
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{{:keys [clients offer]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name params]} offer]
      (-> cofx
          (assoc-in [:db :offer :completed?] false)
          (assoc :fx.webui.offer/list [cimi-client collection-name (utils/prepare-params params)])))))

;; usage:  (dispatch [:set-offer-search [params]])
(reg-event-fx
  :set-offer
  [db/debug-interceptors trim-v]
  (fn [cofx [url-params]]
    (let [{{:keys [clients offer]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name params]} offer
          new-params (utils/merge-offer-params params url-params)]
      (-> cofx
          (assoc-in [:db :offer :params] new-params)
          (assoc-in [:db :resource-path] ["offer"])
          (assoc :fx.webui.offer/list [cimi-client collection-name (utils/prepare-params new-params)])))))

;; usage:  (dispatch [:set-offer-detail [uuid]])
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

;; usage:  (dispatch [:show-offer-table])
(reg-event-fx
  :show-offer-table
  [db/debug-interceptors]
  (fn [cofx []]
    (-> cofx
        (assoc-in [:db :offer-data] nil)
        (assoc :fx.webui.history/navigate ["offer"]))))

