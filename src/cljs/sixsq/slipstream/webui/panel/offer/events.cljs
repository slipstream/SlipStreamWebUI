(ns sixsq.slipstream.webui.panel.offer.events
  (:require
    [clojure.set :as set]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.main.db :as db]
    [sixsq.slipstream.webui.utils :as utils]))

;; usage: (dispatch [:set-offer-data data])
(reg-event-db
  :set-offer-data
  [db/check-spec-interceptor trim-v]
  (fn [db [data]]
    (assoc db :offer-data data)))

;; usage: (dispatch [:clear-offer-data data])
(reg-event-db
  :clear-offer-data
  [db/check-spec-interceptor]
  (fn [db _]
    (assoc db :offer-data nil)))

;; usage:  (dispatch [:show-offer-results results])
;; shows the offer results
(reg-event-db
  :show-offer-results
  [db/check-spec-interceptor trim-v]
  (fn [db [resource-type results]]
    (let [entries (get results (keyword resource-type) [])
          fields (utils/merge-keys (conj entries {:id "id"}))]
      (-> db
          (update-in [:offer :results] (constantly results))
          (update-in [:offer :completed?] (constantly true))
          (update-in [:offer :available-fields] (constantly fields))))))

;; usage:  (dispatch [:set-offer-first f])
(reg-event-db
  :set-offer-first
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 1)]
      (assoc-in db [:offer :params :$first] n))))

;; usage:  (dispatch [:set-offer-last f])
(reg-event-db
  :set-offer-last
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (assoc-in db [:offer :params :$last] n))))

;; usage:  (dispatch [:set-offer-filter f])
(reg-event-db
  :set-offer-filter
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (assoc-in db [:offer :params :$filter] v)))

;; usage:  (dispatch [:set-selected-fields fields])
(reg-event-db
  :set-offer-selected-fields
  [db/check-spec-interceptor trim-v]
  (fn [db [fields]]
    (assoc-in db [:offer :selected-fields] (set/union #{"id"} fields))))

;; usage:  (dispatch [:remove-selected-field field])
(reg-event-db
  :remove-offer-selected-field
  [db/check-spec-interceptor trim-v]
  (fn [db [field]]
    (update-in db [:offer :selected-fields] #(set/difference % #{field}))))

;; usage:  (dispatch [:offer])
;; refine offer
(reg-event-fx
  :offer
  [db/check-spec-interceptor]
  (fn [cofx _]
    (let [{{:keys [clients offer]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name params]} offer]
      (-> cofx
          (assoc-in [:db :offer :completed?] false)
          (assoc :cimi/offer [cimi-client collection-name (utils/prepare-params params)])))))

;; usage:  (dispatch [:set-offer-search [params]])
(reg-event-fx
  :set-offer
  [db/check-spec-interceptor trim-v]
  (fn [cofx [url-params]]
    (let [{{:keys [clients offer]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name params]} offer
          new-params (utils/merge-offer-params params url-params)]
      (-> cofx
          (assoc-in [:db :offer :params] new-params)
          (assoc-in [:db :resource-path] ["offer"])
          (assoc :cimi/offer [cimi-client collection-name (utils/prepare-params new-params)])))))

;; usage:  (dispatch [:set-offer-detail [uuid]])
(reg-event-fx
  :set-offer-detail
  [db/check-spec-interceptor trim-v]
  (fn [cofx [uuid]]
    (let [{{:keys [clients offer]} :db} cofx
          cimi-client (:cimi clients)
          {:keys [collection-name]} offer]
      (-> cofx
          (assoc-in [:db :resource-path] ["offer" uuid])
          (assoc :cimi/offer-detail [cimi-client collection-name uuid])))))

;; usage:  (dispatch [:show-offer-table])
(reg-event-fx
  :show-offer-table
  [db/check-spec-interceptor]
  (fn [cofx []]
    (-> cofx
        (assoc-in [:db :offer-data] nil)
        (assoc :navigate ["offer"]))))

