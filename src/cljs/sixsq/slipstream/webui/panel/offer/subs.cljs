(ns sixsq.slipstream.webui.panel.offer.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :offer-data
         (fn [db _] (-> db :offer-data)))

(reg-sub :offer
         (fn [db _] (-> db :offer)))

(reg-sub :offer-completed?
         (fn [db _] (-> db :offer :completed?)))

(reg-sub :offer-results
         (fn [db _] (-> db :offer :results)))

(reg-sub :offer-params
         (fn [db _] (-> db :offer :params)))

(reg-sub :offer-collection-name
         (fn [db _] (-> db :offer :collection-name)))

(reg-sub :offer-available-fields
         (fn [db _] (-> db :offer :available-fields)))

(reg-sub :offer-selected-fields
         (fn [db _] (-> db :offer :selected-fields)))
