(ns sixsq.slipstream.webui.panel.app.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [taoensso.timbre :as log]))

(reg-event-db
  :set-modules-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (assoc db :modules-data data)))

(reg-event-fx
  :modules-search
  [db/debug-interceptors]
  (fn [{{:keys [clients resource-path]} :db :as cofx} _]
    (let [client (:modules clients)
          module-href (some->> resource-path rest seq (str/join "/"))]
      (log/info "searching modules for " module-href)
      (assoc cofx :fx.webui.app/search [client module-href]))))
