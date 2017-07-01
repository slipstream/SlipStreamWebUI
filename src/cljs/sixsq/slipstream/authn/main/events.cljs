(ns sixsq.slipstream.authn.main.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.main.db :as db]
    [taoensso.timbre :as log]))

(reg-event-db
  :evt.authn.main/redirect
  [db/check-spec-interceptor]
  (fn [db _]
    (log/info "redirecting to /dashboard")
    (.assign (.-location js/window) "/dashboard")
    db))

(reg-event-fx
  :evt.authn.main/trigger-redirect
  [db/check-spec-interceptor]
  (fn [cofx _]
    (log/info "scheduling redirect to /dashboard")
    (assoc cofx :dispatch-later [{:ms 200 :dispatch [:evt.authn.main/redirect]}])))
