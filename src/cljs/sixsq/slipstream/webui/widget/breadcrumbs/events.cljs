(ns sixsq.slipstream.webui.widget.breadcrumbs.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.widget.breadcrumbs.utils :as u]))

(reg-event-fx
  :trim-breadcrumbs
  [db/debug-interceptors trim-v]
  (fn [cofx [n]]
    (let [breadcrumbs (->> cofx
                           :db
                           :modules-breadcrumbs
                           vec
                           (take n)
                           (concat ["application"]))
          app-url (u/breadcrumbs->url breadcrumbs)]
      (assoc cofx :fx.webui.history/navigate [app-url]))))

(reg-event-fx
  :push-breadcrumb
  [db/debug-interceptors trim-v]
  (fn [cofx [crumb]]
    (let [breadcrumbs (-> cofx
                          :db
                          :modules-breadcrumbs
                          vec
                          (conj crumb))
          app-url (u/breadcrumbs->url (concat ["application"] breadcrumbs))]
      (assoc cofx :fx.webui.history/navigate [app-url]))))

(reg-event-fx
  :set-breadcrumbs
  [db/debug-interceptors trim-v]
  (fn [cofx [breadcrumbs]]
    (let [client (get-in cofx [:db :clients :modules])]
      (-> cofx
          (assoc-in [:db :modules-breadcrumbs] breadcrumbs)
          (assoc-in [:db :resource-path] (concat ["application"] breadcrumbs))
          (assoc :fx.webui.app/search [client (u/breadcrumbs->url breadcrumbs)])))))
