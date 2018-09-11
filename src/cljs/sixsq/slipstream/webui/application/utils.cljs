(ns sixsq.slipstream.webui.application.utils
  (:require
    [clojure.string :as str]))

(defn nav-path->module-path
  [nav-path]
  (some->> nav-path rest seq (str/join "/")))
