(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.application.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]
    [clojure.string :as str]
    [sixsq.slipstream.client.async :as async-client]

    [cubic.application.spec :as spec]
    [cubic.application.effects :as fx]

    [cubic.client.spec :as client-spec]
    [cubic.main.spec :as main-spec]))


(reg-event-db
  ::set-module
  (fn [db [_ module-id module]]
    (assoc db ::spec/completed? true
              ::spec/module-id module-id
              ::spec/module module)))


(reg-event-fx
  ::get-module
  (fn [{{:keys [::client-spec/client ::main-spec/nav-path] :as db} :db} _]
    (when client
      (let [path (some->> nav-path rest seq (str/join "/"))]
        {:db             (assoc db ::spec/completed? false
                                   ::spec/module-id nil
                                   ::spec/module nil)
         ::fx/get-module [client path]}))))
