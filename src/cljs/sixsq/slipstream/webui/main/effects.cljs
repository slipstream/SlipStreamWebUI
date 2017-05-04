(ns sixsq.slipstream.webui.main.effects
  (:require
    [cljs.pprint :refer [pprint]]
    [re-frame.core :refer [reg-fx]]))

;; trace events that are dispatched
(reg-fx
  :event
  (fn [[arg]]
    nil
    ;(.log js/console "Event: " (with-out-str (pprint arg)))
    ))

;; FIXME: should not need to do this!
(reg-fx
  :re-frame.std-interceptors/untrimmed-event
  (fn [_]
    nil))
