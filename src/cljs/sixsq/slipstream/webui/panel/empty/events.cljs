(ns sixsq.slipstream.webui.panel.empty.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.main.db :as db]
    [taoensso.timbre :as log]))

(reg-event-fx
  :evt.webui.empty/redirect-login
  [db/check-spec-interceptor]
  (fn [cofx _]
    (assoc cofx :fx.webui.empty/redirect-login [])))
