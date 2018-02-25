(ns sixsq.slipstream.webui.deployment-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment-detail.spec :as deployment-detail-spec]))


(reg-sub
  ::runUUID
  (fn [db]
    (::deployment-detail-spec/runUUID db)))


(reg-sub
  ::reports
  (fn [db]
    (::deployment-detail-spec/reports db)))


(reg-sub
  ::loading?
  (fn [db]
    (::deployment-detail-spec/loading? db)))


(reg-sub
  ::cached-resource-id
  (fn [db]
    (::deployment-detail-spec/cached-resource-id db)))


(reg-sub
  ::resource
  (fn [db]
    (::deployment-detail-spec/resource db)))


(reg-sub
  ::events
  (fn [db]
    (::deployment-detail-spec/events db)))
