(ns sixsq.slipstream.webui.main.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.pprint :refer [pprint]]
    [taoensso.timbre :as log]
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.webui.main.utils :as utils]
    [sixsq.slipstream.client.api.cimi :as cimi]))

;; trace events that are dispatched
(reg-fx
  :event
  (fn [[arg]]
    (log/debug "Event:" (with-out-str (pprint arg)))))

;; FIXME: should not need to do this!
(reg-fx
  :re-frame.std-interceptors/untrimmed-event
  (fn [_]
    nil))

(reg-fx
  :fx.webui.main/set-host-theme
  (fn [[arg]]
    (utils/add-host-theme)))
