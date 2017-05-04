(ns sixsq.slipstream.webui.panel.deployment.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]))

;; usage: (dispatch [:set-runs-data data])
(reg-event-db
  :set-runs-data
  [db/check-spec-interceptor trim-v]
  (fn [db [data]]
    (assoc db :runs-data data)))

;; usage:  (dispatch [:runs-search])
;; refine search
(reg-event-fx
  :runs-search
  [db/check-spec-interceptor]
  (fn [cofx _]
    (let [{:keys [clients runs-params]} (:db cofx)
          client (:runs clients)]
      (-> cofx
          (assoc :runs/search [client runs-params])))))

;; usage:  (dispatch [:set-runs-params {:key value}])
(reg-event-db
  :set-runs-params
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (let [params (:runs-params db)
          new-params (merge params v)]
      (assoc db :runs-params new-params))))

