(ns sixsq.slipstream.webui.panel.deployment.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]))

;; usage: (dispatch [:set-runs-data data])
(reg-event-db
  :evt.webui.deployment/set-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc db :runs-data data)))

;; usage:  (dispatch [:runs-search])
;; refine search
(reg-event-fx
  :evt.webui.deployment/search
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{:keys [clients runs-params]} (:db cofx)
          client (:runs clients)]
      (-> cofx
          (assoc :fx.webui.deployment/search [client runs-params])))))

;; usage:  (dispatch [:set-runs-params {:key value}])
(reg-event-db
  :evt.webui.deployment/set-params
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (let [params (:runs-params db)
          new-params (merge params v)]
      (assoc db :runs-params new-params))))

