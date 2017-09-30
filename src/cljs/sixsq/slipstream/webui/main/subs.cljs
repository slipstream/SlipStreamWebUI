(ns sixsq.slipstream.webui.main.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :message
         (fn [db _] (-> db :message)))

(reg-sub :webui.main/nav-path
         (fn [db _] (-> db :navigation :path)))

(reg-sub :webui.main/nav-query-params
         (fn [db _] (-> db :navigation :query-params)))

(reg-sub :webui.main/cloud-entry-point
         (fn [db _] (-> db :cloud-entry-point)))

(reg-sub :webui.main/alert
         (fn [db _] (-> db :alert)))
