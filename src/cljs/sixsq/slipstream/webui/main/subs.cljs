(ns sixsq.slipstream.webui.main.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.main.spec :as main-spec]))


(reg-sub
  ::sidebar-open?
  (fn [db]
    (::main-spec/sidebar-open? db)))

(reg-sub
  ::visible?
  (fn [db]
    (::main-spec/visible? db)))


(reg-sub
  ::nav-path
  (fn [db]
    (::main-spec/nav-path db)))


(reg-sub
  ::nav-query-params
  (fn [db]
    (::main-spec/nav-query-params db)))

(reg-sub
  ::message
  (fn [db]
    (::main-spec/message db)))
