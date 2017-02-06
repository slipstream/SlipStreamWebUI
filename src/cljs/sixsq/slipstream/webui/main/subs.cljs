(ns sixsq.slipstream.webui.main.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :message
         (fn [db _] (-> db :message)))

(reg-sub :panel
         (fn [db _] (-> db :panel)))

(reg-sub :cloud-entry-point
         (fn [db _] (-> db :cloud-entry-point)))


