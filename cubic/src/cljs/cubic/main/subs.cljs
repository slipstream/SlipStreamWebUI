(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.main.subs
  (:require
    [re-frame.core :refer [reg-sub]]))


(reg-sub
  ::sidebar-open?
  (fn [db]
    (:cubic.main.spec/sidebar-open? db)))


(reg-sub
  ::nav-path
  (fn [db]
    (:cubic.main.spec/nav-path db)))


(reg-sub
  ::nav-query-params
  (fn [db]
    (:cubic.main.spec/nav-query-params db)))
