(ns sixsq.slipstream.webui.application.utils
  (:require
    [clojure.string :as str]))


(defn nav-path->module-path
  [nav-path]
  (some->> nav-path rest seq (str/join "/")))


;; FIXME: Should a pprint option be added to the standard conversion utility in the Clojure client?
(defn edn->json [json]
  (JSON.stringify (clj->js json) nil 2))
