(ns sixsq.slipstream.webui.messages.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.messages.spec :as messages-spec]
    [sixsq.slipstream.webui.utils.time :as time]))


(reg-event-db
  ::show
  (fn [db [_ message]]
    (assoc db ::messages-spec/alert-message message)))


(reg-event-db
  ::hide
  (fn [db _]
    (assoc db ::messages-spec/alert-message nil)))


(reg-event-db
  ::add
  (fn [{:keys [::messages-spec/messages] :as db} [_ message]]
    (let [timestamped-message (assoc message :timestamp (time/now))]
      (dispatch [::show timestamped-message])
      (->> (cons timestamped-message messages)
           vec
           (assoc db ::messages-spec/messages)))))


(reg-event-db
  ::clear-all
  (fn [db _]
    (assoc db ::messages-spec/messages [])))


(reg-event-db
  ::remove
  (fn [{:keys [::messages-spec/messages] :as db} [_ index]]
    (->> (vec (concat (subvec messages 0 index) (subvec messages (inc index))))
         (assoc db ::messages-spec/messages))))
