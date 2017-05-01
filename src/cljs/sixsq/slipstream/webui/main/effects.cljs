(ns sixsq.slipstream.webui.main.effects
  (:require
    [re-frame.core :refer [reg-fx]]
    [sixsq.slipstream.webui.history :as history]))

;; initialize the history with first URL
(reg-fx
  :history/initialize
  (fn [_]
    (history/start)))

;; trace events that are dispatched
(reg-fx
  :event
  (fn [arg]
    (.log js/console "Event: " (with-out-str (cljs.pprint/pprint arg)))))
