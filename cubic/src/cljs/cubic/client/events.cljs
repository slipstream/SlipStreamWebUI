(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.client.events
  (:require
    [re-frame.core :refer [reg-event-db]]
    [sixsq.slipstream.client.async :as async-client]

    [cubic.client.spec :as client-spec]))


(reg-event-db
  ::initialize
  (fn [db [_ slipstream-url]]
    (let [client (async-client/instance (str slipstream-url "/api/cloud-entry-point"))]
      (assoc db ::client-spec/client client))))
