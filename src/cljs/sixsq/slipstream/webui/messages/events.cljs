(ns sixsq.slipstream.webui.messages.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.messages.spec :as messages-spec]
    [sixsq.slipstream.webui.utils.time :as time]))


(reg-event-db
  ::hide
  (fn [db _]
    (assoc db ::messages-spec/alert-message nil
              ::messages-spec/alert-display :none)))


(reg-event-db
  ::close-slider
  (fn [{:keys [::messages-spec/alert-display] :as db} _]
    (if (= :slider alert-display)
      (assoc db ::messages-spec/alert-display :none)
      db)))


(reg-event-db
  ::open-modal
  (fn [db _]
    (assoc db ::messages-spec/alert-display :modal)))


(reg-event-db
  ::close-modal
  (fn [db _]
    (assoc db ::messages-spec/alert-display :none)))


(reg-event-fx
  ::add
  (fn [{{:keys [::messages-spec/messages] :as db} :db :as cofx} [_ message]]
    (let [timestamped-message (assoc message :timestamp (time/now))]
      (let [updated-messages (vec (cons timestamped-message messages))]
        {:db (assoc db ::messages-spec/messages updated-messages
                       ::messages-spec/alert-message timestamped-message
                       ::messages-spec/alert-display :slider)
         :dispatch-later [{:ms 2500 :dispatch [::close-slider]}]}))))


(reg-event-db
  ::clear-all
  (fn [db _]
    (assoc db ::messages-spec/messages []
              ::messages-spec/alert-message nil
              ::messages-spec/alert-display :none)))


(reg-event-db
  ::remove
  (fn [{:keys [::messages-spec/messages] :as db} [_ index]]
    (->> (vec (concat (subvec messages 0 index) (subvec messages (inc index))))
         (assoc db ::messages-spec/messages))))
