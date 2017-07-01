(ns sixsq.slipstream.webui.panel.empty.effects
  (:require
    [re-frame.core :refer [reg-fx]]
    [sixsq.slipstream.webui.widget.history.utils :as history]))

(reg-fx
  :fx.webui.empty/redirect-login
  (fn [_]
    (history/navigate "login")))
