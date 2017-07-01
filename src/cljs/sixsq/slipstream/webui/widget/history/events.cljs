(ns sixsq.slipstream.webui.widget.history.events
  (:require
    [re-frame.core :refer [reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.widget.history.effects]))

(reg-event-fx
  :evt.webui.history/initialize
  [db/debug-interceptors trim-v]
  (fn [cofx [path-prefix]]
    (assoc cofx :fx.webui.history/initialize [path-prefix])))
