(ns sixsq.slipstream.webui.widget.editor.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.widget.i18n.dictionary :as dictionary]
    [re-frame.core :refer [reg-event-db trim-v]]
    [taoensso.timbre :as log]))

(reg-event-db
  :evt.webui.editor/set-data
  [db/debug-interceptors trim-v]
  (fn [db [data]]
    (log/error "DEBUG" "SET-DATA" data)
    (assoc-in db [:editor :data] data)))
