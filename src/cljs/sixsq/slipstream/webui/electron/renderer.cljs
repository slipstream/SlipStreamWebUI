(ns sixsq.slipstream.webui.electron.renderer
  (:require
    [sixsq.slipstream.webui.core :as webui-core]
    [taoensso.timbre :as log]))


(defn ^:export init []
  (log/info "starting electron renderer process...")
  (webui-core/init))
