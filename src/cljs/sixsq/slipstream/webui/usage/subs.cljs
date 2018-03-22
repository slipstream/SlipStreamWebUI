(ns sixsq.slipstream.webui.usage.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.usage.spec :as usage-spec]))

(reg-sub
  ::loading?
  (fn [db]
    (::usage-spec/loading? db)))

(reg-sub
  ::filter-visible?
  (fn [db]
    (::usage-spec/filter-visible? db)))

(reg-sub
  ::results
  (fn [db]
    (::usage-spec/results db)))

(reg-sub
  ::loading-connectors-list?
  (fn [db]
    (::usage-spec/loading-connectors-list? db)))

(reg-sub
  ::connectors-list
  (fn [db]
    (::usage-spec/connectors-list db)))

(reg-sub
  ::loading-users-list?
  (fn [db]
    (::usage-spec/loading-connectors-list? db)))

(reg-sub
  ::users-list
  (fn [db]
    (::usage-spec/users-list db)))

(reg-sub
  ::selected-user
  (fn [db]
    (::usage-spec/selected-user db)))

(reg-sub
  ::date-after
  (fn [db]
    (::usage-spec/date-after db)))

(reg-sub
  ::date-before
  (fn [db]
    (::usage-spec/date-before db)))
