(ns sixsq.slipstream.webui.panel.empty.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]

    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.panel.empty.effects]))

(reg-event-fx
  :evt.webui.empty/redirect-login
  [db/debug-interceptors]
  (fn [cofx _]
    (assoc cofx :fx.webui.empty/redirect-login [])))
