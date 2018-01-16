(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.client.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::client any?)

(s/def ::db (s/keys :req [::client]))

(def defaults {::client nil})
