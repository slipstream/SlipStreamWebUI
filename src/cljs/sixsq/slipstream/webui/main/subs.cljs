(ns sixsq.slipstream.webui.main.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :message
         (fn [db _] (-> db :message)))

(reg-sub :resource-path
         (fn [db _] (-> db :resource-path)))

(reg-sub :resource-query-params
         (fn [db _] (-> db :resource-query-params)))

(reg-sub :cloud-entry-point
         (fn [db _] (-> db :cloud-entry-point)))

(reg-sub :webui.main/alert
         (fn [db _] (-> db :alert)))


