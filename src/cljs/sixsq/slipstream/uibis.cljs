(ns sixsq.slipstream.uibis
  (:require
    [sixsq.slipstream.webui.debug :as debug]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.main :as main]))

(debug/initialize-debugging-tools)
(debug/initialize-logging-level)

(def path-prefix (delay (str (utils/host-url) "/uibis")))

(defn ^:export init
  []
  (main/init @debug/slipstream-url @path-prefix))
