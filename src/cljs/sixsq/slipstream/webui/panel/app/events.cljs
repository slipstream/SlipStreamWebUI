(ns sixsq.slipstream.webui.panel.app.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]))

;; usage: (dispatch [:set-modules-data data])
(reg-event-db
  :set-modules-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc db :modules-data data)))

;; usage:  (dispatch [:modules-search])
;; refine search
(reg-event-fx
  :modules-search
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{:keys [clients modules-path]} (:db cofx)
          client (:modules clients)]
      (-> cofx
          (assoc :fx.webui.app/search [client modules-path])))))

;; usage:  (dispatch [:modules-search])
;; refine search
(reg-event-fx
  :modules-breadcrumbs-search
  [db/debug-interceptors]
  (fn [cofx _]
    (let [{:keys [clients modules-breadcrumbs]} (:db cofx)
          client (:modules clients)]
      (-> cofx
          (assoc :fx.webui.app/search [client (str (rest modules-breadcrumbs))])))))

;; usage:  (dispatch [:set-module-path p])
(reg-event-db
  :set-module-path
  [db/debug-interceptors trim-v]
  (fn [db [v]]
    (assoc db :modules-path v)))
