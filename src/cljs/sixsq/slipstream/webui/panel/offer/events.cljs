(ns sixsq.slipstream.webui.panel.offer.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.utils :as utils]
    [clojure.set :as set]))

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
      (update-in db [:offer :params :$first] (constantly n)))))

;; usage:  (dispatch [:set-offer-last f])
(reg-event-db
  :set-offer-last
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (let [n (or (utils/str->int v) 20)]
      (update-in db [:offer :params :$last] (constantly n)))))

;; usage:  (dispatch [:set-offer-filter f])
(reg-event-db
  :set-offer-filter
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (update-in db [:offer :params :$filter] (constantly v))))

;; usage:  (dispatch [:set-selected-fields fields])
(reg-event-db
  :set-offer-selected-fields
  [db/check-spec-interceptor trim-v]
  (fn [db [fields]]
    (update-in db [:offer :selected-fields] (constantly (set/union #{"id"} fields)))))

;; usage:  (dispatch [:remove-selected-field field])
(reg-event-db
  :remove-offer-selected-field
  [db/check-spec-interceptor trim-v]
  (fn [db [field]]
    (update-in db [:offer :selected-fields] #(set/difference % #{field}))))

;; usage:  (dispatch [:switch-offer-resource resource-type])
;; trigger offer on new resource type
(reg-event-fx
  :new-offer
  [db/check-spec-interceptor trim-v]
  (fn [cofx [new-collection-name]]
    (let [cofx (assoc-in cofx [:db :offer :collection-name] new-collection-name)
          {:keys [client offer]} (:db cofx)
          {:keys [collection-name params]} offer]
      (-> cofx
          (update-in [:db :offer :completed?] (constantly false))
          (assoc :cimi/offer [client collection-name (utils/prepare-params params)])))))

;; usage:  (dispatch [:offer])
;; refine offer
(reg-event-fx
  :offer
  [db/check-spec-interceptor]
  (fn [cofx _]
    (let [{:keys [client offer]} (:db cofx)
          {:keys [collection-name params]} offer]
      (-> cofx
          (update-in [:db :offer :completed?] (constantly false))
          (assoc :cimi/offer [client collection-name (utils/prepare-params params)])))))

