(ns sixsq.slipstream.webui.widget.accordeon-menu.events
  (:require [re-frame.core :as rf]
            [sixsq.slipstream.webui.db :as db]))

(rf/reg-event-db
  :main-menu/display
  (fn [db [_ display]]
    (assoc-in db [:main-menu/display] display)))

;(rf/reg-event-db
; :initialize-db
; (fn  [_ _]
;   db/app-db))
