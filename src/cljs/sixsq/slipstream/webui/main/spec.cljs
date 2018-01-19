(ns sixsq.slipstream.webui.main.spec
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::sidebar-open? boolean?)

(s/def ::nav-path any?)

(s/def ::nav-query-params any?)

(s/def ::db (s/keys :req [::sidebar-open? ::nav-path ::nav-query-params]))

(def defaults {::sidebar-open?    true
               ::nav-path         ["cimi"]
               ::nav-query-params {}})
