(ns sixsq.slipstream.authn.main.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.main.db :as db]
    [sixsq.slipstream.authn.main.effects :as effects]
    [taoensso.timbre :as log]))

(reg-event-db
  :evt.authn.main/redirect
  [db/check-spec-interceptor]
  (fn [db _]
    (log/info "redirecting to /dashboard")
    (aset js/window "location" "/dashboard")
    db))

(reg-event-fx
  :evt.authn.main/trigger-redirect
  [db/check-spec-interceptor]
  (fn [cofx _]
    (log/info "triggering redirect effect")
    (assoc cofx :fx.authn.main/trigger-redirect [])))
