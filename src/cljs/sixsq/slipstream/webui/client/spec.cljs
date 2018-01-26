(ns sixsq.slipstream.webui.client.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::client any?)

(s/def ::db (s/keys :req [::client]))

(def defaults {::client nil})
