(ns sixsq.slipstream.webui.data.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.data.spec :as data-spec]))


(reg-sub
  ::query-params
  (fn [db _]
    (::data-spec/query-params db)))


(reg-sub
  ::aggregations
  (fn [db _]
    (::data-spec/aggregations db)))


(reg-sub
  ::collection
  (fn [db _]
    (::data-spec/collection db)))


(reg-sub
  ::collection-name
  (fn [db _]
    (::data-spec/collection-name db)))


(reg-sub
  ::selected-fields
  (fn [db _]
    (::data-spec/selected-fields db)))


(reg-sub
  ::available-fields
  (fn [db _]
    (::data-spec/available-fields db)))


(reg-sub
  ::cloud-entry-point
  (fn [db _]
    (::data-spec/cloud-entry-point db)))


(reg-sub
  ::show-add-modal?
  (fn [db _]
    (::data-spec/show-add-modal? db)))


(reg-sub
  ::descriptions-vector
  (fn [db _]
    (::data-spec/descriptions-vector db)))


(reg-sub
  ::loading?
  (fn [db]
    (::data-spec/loading? db)))


(reg-sub
  ::filter-visible?
  (fn [db]
    (::data-spec/filter-visible? db)))


