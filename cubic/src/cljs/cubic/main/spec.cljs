(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.main.spec
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::sidebar-open? boolean?)

(s/def ::nav-path any?)

(s/def ::nav-query-params any?)

(s/def ::db (s/keys :req [::sidebar-open? ::nav-path ::nav-query-params]))

(def defaults {::sidebar-open?    true
               ::nav-path         ["cimi"]
               ::nav-query-params {}})
