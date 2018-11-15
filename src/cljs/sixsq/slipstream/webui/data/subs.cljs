(ns sixsq.slipstream.webui.data.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.data.spec :as spec]))


(reg-sub
  ::time-period
  (fn [db]
    (::spec/time-period db)))


(reg-sub
  ::time-period-filter
  (fn [db]
    (::spec/time-period-filter db)))


;; unused, all information taken when fetching content-types
;;(reg-sub
;;  ::service-offers
;;  (fn [db]
;;    (::spec/service-offers db)))


(reg-sub
  ::credentials
  (fn [db]
    (::spec/credentials db)))


(reg-sub
  ::cloud-filter
  (fn [db]
    (::spec/cloud-filter db)))


(reg-sub
  ::content-types
  (fn [db]
    (::spec/content-types db)))


(reg-sub
  ::application-select-visible?
  (fn [db]
    (::spec/application-select-visible? db)))


(reg-sub
  ::loading-applications?
  (fn [db]
    (::spec/loading-applications? db)))


(reg-sub
  ::applications
  (fn [db]
    (::spec/applications db)))



(reg-sub
  ::gnss-filter
  (fn [db]
    (::spec/gnss-filter db)))


