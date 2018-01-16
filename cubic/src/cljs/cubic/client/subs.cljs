(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.client.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [cubic.client.spec :as client-spec]))


(reg-sub
  ::client
  (fn [db]
    (::client-spec/client db)))
