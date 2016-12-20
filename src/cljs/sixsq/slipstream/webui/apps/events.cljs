(ns sixsq.slipstream.webui.apps.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [clojure.string :as str]))

;; usage: (dispatch [:set-modules-data data])
(reg-event-db
  :set-modules-data
  [db/check-spec-interceptor trim-v]
  (fn [db [data]]
    (assoc db :modules-data data)))

;; usage:  (dispatch [:modules-search])
;; refine search
(reg-event-fx
  :modules-search
  [db/check-spec-interceptor]
  (fn [cofx _]
    (let [{:keys [clients modules-path]} (:db cofx)
          client (:modules clients)]
      (-> cofx
          (assoc :modules/search [client modules-path])))))

;; usage:  (dispatch [:modules-search])
;; refine search
(reg-event-fx
  :modules-breadcrumbs-search
  [db/check-spec-interceptor]
  (fn [cofx _]
    (let [{:keys [clients modules-breadcrumbs]} (:db cofx)
          client (:modules clients)]
      (-> cofx
          (assoc :modules/search [client (str/join (rest modules-breadcrumbs))])))))

;; usage:  (dispatch [:set-module-path p])
(reg-event-db
  :set-module-path
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (assoc db :modules-path v)))

;; usage:  (dispatch [:trim-breadcrumbs n])
(reg-event-db
  :trim-breadcrumbs
  [db/check-spec-interceptor trim-v]
  (fn [db [n]]
    (update-in db :modules-breadcrumbs (partial take n))))

;; usage:  (dispatch [:push-breadcrumb crumb])
(reg-event-db
  :push-breadcrumb
  [db/check-spec-interceptor trim-v]
  (fn [db [crumb]]
    (let [breadcrumbs (vec (:modules-breadcrumbs db))]
      (assoc db :modules-breadcrumbs (conj breadcrumbs crumb)))))
