(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.i18n.subs
  (:require
    [re-frame.core :refer [reg-sub]]))


(reg-sub
  ::locale
  (fn [db]
    (:cubic.i18n.spec/locale db)))


(reg-sub
  ::tr
  (fn [db]
    (:cubic.i18n.spec/tr db)))
