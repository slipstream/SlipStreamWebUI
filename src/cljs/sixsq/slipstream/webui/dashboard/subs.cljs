(ns sixsq.slipstream.webui.dashboard.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.dashboard.spec :as dashboard-spec]))


(reg-sub
  ::statistics
  (fn [db]
    (::dashboard-spec/statistics db)))


(reg-sub
  ::loading?
  (fn [db]
    (::dashboard-spec/loading? db)))

(reg-sub
  ::selected-tab
  (fn [db]
    (::dashboard-spec/selected-tab db)))

(reg-sub
  ::virtual-machines
  (fn [db]
    (::dashboard-spec/virtual-machines db)))

(reg-sub
  ::deployments
  (fn [db]
    (::dashboard-spec/deployments db)))

(reg-sub
  ::records-displayed
  (fn [db]
    (::dashboard-spec/records-displayed db)))

(reg-sub
  ::page
  (fn [db]
    (::dashboard-spec/page db)))

(reg-sub
  ::total-pages
  (fn [db]
    (::dashboard-spec/total-pages db)))

(reg-sub
  ::active-deployments-only
  (fn [db]
    (::dashboard-spec/active-deployments-only db)))

(reg-sub
  ::deleted-deployments
  (fn [db]
    (::dashboard-spec/deleted-deployments db)))

(reg-sub
  ::delete-deployment-modal
  (fn [db]
    (::dashboard-spec/delete-deployment-modal db)))

(reg-sub
  ::error-message-deployment
  (fn [db]
    (::dashboard-spec/error-message-deployment db)))

(reg-sub
  ::loading-tab?
  (fn [db]
    (::dashboard-spec/loading-tab? db)))
