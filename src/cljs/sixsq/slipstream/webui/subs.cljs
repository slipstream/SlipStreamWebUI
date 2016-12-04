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

(defn resource-data
  [db _]
  (:resource-data db))
(reg-sub :resource-data resource-data)

(defn cloud-entry-point
  [db _]
  (:cloud-entry-point db))
(reg-sub :cloud-entry-point cloud-entry-point)

(defn search
  [db _]
  (get-in db [:search]))
(reg-sub :search search)

(defn search-completed?
  [db _]
  (get-in db [:search :completed?]))
(reg-sub :search-completed? search-completed?)

(defn search-results
  [db _]
  (get-in db [:search :results]))
(reg-sub :search-results search-results)

(defn search-params
  [db _]
  (get-in db [:search :params]))
(reg-sub :search-params search-params)

(defn search-collection-name
  [db _]
  (get-in db [:search :collection-name]))
(reg-sub :search-collection-name search-collection-name)

(defn search-available-fields
  [db _]
  (get-in db [:search :available-fields]))
(reg-sub :search-available-fields search-available-fields)

(defn search-selected-fields
  [db _]
  (get-in db [:search :selected-fields]))
(reg-sub :search-selected-fields search-selected-fields)

