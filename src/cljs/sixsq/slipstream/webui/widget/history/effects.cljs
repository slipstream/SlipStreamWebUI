(ns sixsq.slipstream.webui.widget.history.effects
  (:require
    [re-frame.core :refer [reg-fx]]
    [sixsq.slipstream.webui.widget.history.utils :as hutils]))

(reg-fx
  :fx.webui.history/initialize
  (fn [[path-prefix]]
    (hutils/initialize path-prefix)
    (hutils/start path-prefix)))

(reg-fx
  :fx.webui.history/navigate
  (fn [[url]]
    (hutils/navigate url)))
