(ns sixsq.slipstream.webui.cimi.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]))


(reg-sub
  ::query-params
  (fn [db _]
    (::cimi-spec/query-params db)))


(reg-sub
  ::aggregations
  (fn [db _]
    (::cimi-spec/aggregations db)))


(reg-sub
  ::collection
  (fn [db _]
    (::cimi-spec/collection db)))


(reg-sub
  ::collection-name
  (fn [db _]
    (::cimi-spec/collection-name db)))


(reg-sub
  ::selected-fields
  (fn [db _]
    (::cimi-spec/selected-fields db)))


(reg-sub
  ::available-fields
  (fn [db _]
    (::cimi-spec/available-fields db)))


(reg-sub
  ::cloud-entry-point
  (fn [db _]
    (::cimi-spec/cloud-entry-point db)))


(reg-sub
  ::show-add-modal?
  (fn [db _]
    (::cimi-spec/show-add-modal? db)))


(reg-sub
  ::descriptions-vector
  (fn [db _]
    (::cimi-spec/descriptions-vector db)))


(reg-sub
  ::loading?
  (fn [db]
    (::cimi-spec/loading? db)))


(reg-sub
  ::filter-visible?
  (fn [db]
    (::cimi-spec/filter-visible? db)))


