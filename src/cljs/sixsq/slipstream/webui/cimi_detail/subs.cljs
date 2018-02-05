(ns sixsq.slipstream.webui.cimi-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.cimi-detail.spec :as cimi-detail-spec]))


(reg-sub
  ::loading?
  (fn [db]
    (::cimi-detail-spec/loading? db)))


(reg-sub
  ::resource-id
  (fn [db _]
    (::cimi-detail-spec/resource-id db)))


(reg-sub
  ::resource
  (fn [db _]
    (::cimi-detail-spec/resource db)))


(reg-sub
  ::description
  (fn [db _]
    (::cimi-detail-spec/description db)))
