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

