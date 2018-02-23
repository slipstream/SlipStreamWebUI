(ns sixsq.slipstream.webui.deployment-detail.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment-detail.spec :as deployment-detail-spec]))


(reg-sub
  ::loading?
  (fn [db]
    (::deployment-detail-spec/loading? db)))

(reg-sub
  ::runUUID
  (fn [db]
    (::deployment-detail-spec/runUUID db)))

(reg-sub
  ::reports
  (fn [db]
    (::deployment-detail-spec/reports db)))
