(ns sixsq.slipstream.webui.panel.offer.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :offer-data
         (fn [db _] (-> db :offer :cache :resource)))

(reg-sub :offer
         (fn [db _] (-> db :offer)))

(reg-sub :offer-completed?
         (fn [db _] (-> db :offer :completed?)))

(reg-sub :offer-listing
         (fn [db _] (-> db :offer :cache :resources)))

(reg-sub :offer-params
         (fn [db _] (-> db :offer :query-params)))

(reg-sub :offer-params-first
         (fn [db _] (get-in db [:offer :query-params :$first])))

(reg-sub :offer-params-last
         (fn [db _] (get-in db [:offer :query-params :$last])))

(reg-sub :offer-params-filter
         (fn [db _] (get-in db [:offer :query-params :$filter])))

(reg-sub :offer-collection-name
         (fn [db _] (-> db :offer :collection-name)))

(reg-sub :offer-available-fields
         (fn [db _] (->> db :offer :fields :available (map (fn [v] {:id v :label v})))))

(reg-sub :offer-selected-fields
         (fn [db _] (-> db :offer :fields :selected)))
