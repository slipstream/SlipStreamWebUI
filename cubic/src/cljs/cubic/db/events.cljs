(ns cubic.db.events
  (:require
    [re-frame.core :refer [reg-event-db]]
    [cubic.db.spec :as db]))


(reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))
