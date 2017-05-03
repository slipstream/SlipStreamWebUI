(ns sixsq.slipstream.webui.widget.history.events
  (:require
    [re-frame.core :refer [reg-event-fx trim-v]]
    [sixsq.slipstream.webui.main.db :as db]))

;; usage:  (dispatch [:initialize-history path-prefix])
;; triggers initial entry in application history
(reg-event-fx
  :initialize-history
  [db/check-spec-interceptor trim-v]
  (fn [{:keys [db]} [path-prefix]]
    {:db db
     :history/initialize [path-prefix]}))
