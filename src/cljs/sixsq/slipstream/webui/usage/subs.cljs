(ns sixsq.slipstream.webui.usage.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::loading?
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/loading? db)))

(reg-sub
  ::results
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/results db)))

(reg-sub
  ::loading-connectors-list?
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/loading-connectors-list? db)))

(reg-sub
  ::connectors-list
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/connectors-list db)))

(reg-sub
  ::loading-users-list?
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/loading-connectors-list? db)))

(reg-sub
  ::users-list
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/users-list db)))

(reg-sub
  ::selected-user
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/selected-user db)))

(reg-sub
  ::date-after
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/date-after db)))

(reg-sub
  ::date-before
  (fn [db]
    (:sixsq.slipstream.webui.usage.spec/date-before db)))
