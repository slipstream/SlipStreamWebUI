(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.authn.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [cubic.authn.spec :as authn-spec]))


(reg-sub
  ::modal-open?
  (fn [db]
    (::authn-spec/modal-open? db)))

(reg-sub
  ::session
  (fn [db]
    (::authn-spec/session db)))

(reg-sub
  ::error-message
  (fn [db]
    (::authn-spec/error-message db)))

(reg-sub
  ::total
  (fn [db]
    (::authn-spec/total db)))

(reg-sub
  ::count
  (fn [db]
    (::authn-spec/count db)))

(reg-sub
  ::methods
  (fn [db]
    (::authn-spec/methods db)))

(reg-sub
  ::redirect-uri
  (fn [db]
    (::authn-spec/redirect-uri db)))

(reg-sub
  ::server-redirect-uri
  (fn [db]
    (::authn-spec/server-redirect-uri db)))
