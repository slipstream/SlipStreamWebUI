(ns sixsq.slipstream.webui.panel.cimi.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :resource-data
         (fn [db _] (-> db :resource-data)))

(reg-sub :search
         (fn [db _] (-> db :search)))

(reg-sub :search-completed?
         (fn [db _] (-> db :search :completed?)))

(reg-sub :search-listing
         (fn [db _] (-> db :search :listing)))

(reg-sub :search-params
         (fn [db _] (-> db :search :params)))

(reg-sub :search-collection-name
         (fn [db _] (-> db :search :collection-name)))

(reg-sub :search-available-fields
         (fn [db _] (-> db :search :available-fields)))

(reg-sub :search-selected-fields
         (fn [db _] (-> db :search :selected-fields)))
