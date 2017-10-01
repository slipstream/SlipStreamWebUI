(ns sixsq.slipstream.webui.panel.app.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [taoensso.timbre :as log]))

(reg-event-db
  :evt.webui.app/set-modules-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc db :modules-data data)))

(reg-event-fx
  :evt.webui.app/modules-search
  [db/debug-interceptors]
  (fn [cofx _]
    (let [resource-path (-> cofx :db :navigation :path)
          client (-> cofx :db :clients :cimi)
          module-href (some->> resource-path rest seq (str/join "/"))]
      (log/info "searching modules for" module-href)
      (-> cofx
          (assoc-in [:db :modules-data] nil)
          (assoc :fx.webui.app/search [client module-href])))))
