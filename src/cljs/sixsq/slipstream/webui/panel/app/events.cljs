(ns sixsq.slipstream.webui.panel.app.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.main.db :as db]))

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
          (assoc :modules/search [client (str (rest modules-breadcrumbs))])))))

;; usage:  (dispatch [:set-module-path p])
(reg-event-db
  :set-module-path
  [db/check-spec-interceptor trim-v]
  (fn [db [v]]
    (assoc db :modules-path v)))

;; FIXME: MOVE TO UTILITIES!
(defn breadcrumbs->url [crumbs]
  (if (seq crumbs)
    (str/join "/" crumbs)))

;; usage:  (dispatch [:trim-breadcrumb n])
(reg-event-fx
  :trim-breadcrumbs
  [db/check-spec-interceptor trim-v]
  (fn [cofx [n]]
    (let [breadcrumbs (->> cofx
                           :db
                           :modules-breadcrumbs
                           vec
                           (take n)
                           (concat ["app"]))
          app-url (breadcrumbs->url breadcrumbs)]
      (assoc cofx :navigate [app-url]))))

;; usage:  (dispatch [:push-breadcrumb crumb])
(reg-event-fx
  :push-breadcrumb
  [db/check-spec-interceptor trim-v]
  (fn [cofx [crumb]]
    (let [breadcrumbs (-> cofx
                          :db
                          :modules-breadcrumbs
                          vec
                          (conj crumb))
          app-url (breadcrumbs->url (concat ["app"] breadcrumbs))]
      (assoc cofx :navigate [app-url]))))

;; usage:  (dispatch [:set-breadcrumbs [breadcrumbs]])
(reg-event-fx
  :set-breadcrumbs
  [db/check-spec-interceptor trim-v]
  (fn [cofx [breadcrumbs]]
    (let [client (get-in cofx [:db :clients :modules])]
      (-> cofx
          (assoc-in [:db :modules-breadcrumbs] breadcrumbs)
          (assoc-in [:db :resource-path] (concat ["app"] breadcrumbs))
          (assoc :modules/search [client (breadcrumbs->url breadcrumbs)])))))
