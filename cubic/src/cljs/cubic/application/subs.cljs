(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.application.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [cubic.application.spec :as application-spec]))


(reg-sub
  ::completed?
  (fn [db]
    (::application-spec/completed? db)))


(reg-sub
  ::module-id
  (fn [db _]
    (::application-spec/module-id db)))


(reg-sub
  ::module
  (fn [db _]
    (::application-spec/module db)))
