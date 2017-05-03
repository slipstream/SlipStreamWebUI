(ns sixsq.slipstream.webui.main.effects
  (:require
    [cljs.pprint :refer [pprint]]
    [re-frame.core :refer [reg-fx]]))

;; trace events that are dispatched
(reg-fx
  :event
  (fn [[arg]]
    (.log js/console "Event: " (with-out-str (pprint arg)))))
