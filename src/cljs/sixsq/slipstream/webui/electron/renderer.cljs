(ns sixsq.slipstream.webui.electron.renderer
  (:require
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.core :as webui-core]))


(defn ^:export init []
  (log/info "starting electron renderer process...")
  (webui-core/init))
