(ns sixsq.slipstream.webui.widget.history.events
  (:require
    [re-frame.core :refer [reg-event-fx trim-v]]
    [sixsq.slipstream.webui.main.db :as db]
    [sixsq.slipstream.webui.widget.history.effects]))

(reg-event-fx
  :evt.webui.history/initialize
  [db/check-spec-interceptor trim-v]
  (fn [cofx [path-prefix]]
    (assoc cofx :fx.webui.history/initialize [path-prefix])))
