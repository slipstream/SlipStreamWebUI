(ns sixsq.slipstream.webui.panel.cimi.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :webui.cimi/resource-data
         (fn [db _] (-> db :search :cache :resource)))

(reg-sub :search
         (fn [db _] (-> db :search)))

(reg-sub :search-completed?
         (fn [db _] (-> db :search :completed?)))

(reg-sub :webui.cimi/aggregations
         (fn [db _] (-> db :search :cache :aggregations)))

(reg-sub :search-listing
         (fn [db _] (-> db :search :cache :resources)))

(reg-sub :search-params
         (fn [db _] (-> db :search :query-params)))

(reg-sub :webui.cimi/collection-name
         (fn [db _] (-> db :search :collection-name)))

(reg-sub :search-available-fields
         (fn [db _] (->> db :search :fields :available (map (fn [v] {:id v :label v})))))

(reg-sub :search-selected-fields
         (fn [db _] (-> db :search :fields :selected)))

(reg-sub :webui.cimi/show-modal?
         (fn [db _] (-> db :search :show-modal?)))

(reg-sub :webui.cimi/descriptions-vector
         (fn [db _] (->> db :search :descriptions vals (sort :id))))
