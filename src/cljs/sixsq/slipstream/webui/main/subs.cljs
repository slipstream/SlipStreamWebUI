(ns sixsq.slipstream.webui.main.subs
  (:require [re-frame.core :refer [reg-sub]]))

(defn message
  [db _]
  (:message db))
(reg-sub :message message)

(defn panel
  [db _]
  (get-in db [:panel]))
(reg-sub :panel panel)

(defn cloud-entry-point
  [db _]
  (:cloud-entry-point db))
(reg-sub :cloud-entry-point cloud-entry-point)


