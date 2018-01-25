(ns sixsq.slipstream.webui.main.subs
  (:require
    [re-frame.core :refer [reg-sub]]))


(reg-sub
  ::sidebar-open?
  (fn [db]
    (:sixsq.slipstream.webui.main.spec/sidebar-open? db)))

(reg-sub
  ::visible?
  (fn [db]
    (:sixsq.slipstream.webui.main.spec/visible? db)))


(reg-sub
  ::nav-path
  (fn [db]
    (:sixsq.slipstream.webui.main.spec/nav-path db)))


(reg-sub
  ::nav-query-params
  (fn [db]
    (:sixsq.slipstream.webui.main.spec/nav-query-params db)))

(reg-sub
  ::message
  (fn [db]
    (:sixsq.slipstream.webui.main.spec/message db)))
