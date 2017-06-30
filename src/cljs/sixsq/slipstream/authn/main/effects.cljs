(ns sixsq.slipstream.authn.main.effects
  (:require
    [re-frame.core :refer [reg-event-fx]]
    [sixsq.slipstream.webui.main.db :as db]
    [taoensso.timbre :as log]))

(reg-event-fx
  :fx.authn.main/trigger-redirect
  [db/check-spec-interceptor]
  (fn [cofx _]
    (log/info "triggering delayed redirect dispatch")
    (assoc cofx :dispatch-later [{:ms 2000 :dispatch [:evt.authn.main/redirect]}])))
