(ns sixsq.slipstream.webui.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(defn logged-in?
  [db _]
  (get-in db [:authn :logged-in?]))
(reg-sub :logged-in? logged-in?)

(defn authn
  [db _]
  (:authn db))
(reg-sub :authn authn)

(defn message
  [db _]
  (:message db))
(reg-sub :message message)

(defn cloud-entry-point
  [db _]
  (:cloud-entry-point db))
(reg-sub :cloud-entry-point cloud-entry-point)

(defn resource-url
  [db _]
  (:resource-url db))
(reg-sub :resource-url resource-url)

(defn results
  [db _]
  (:results db))
(reg-sub :results results)
