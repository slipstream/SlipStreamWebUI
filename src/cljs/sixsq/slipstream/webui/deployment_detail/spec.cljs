(ns sixsq.slipstream.webui.deployment-detail.spec
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::runUUID (s/nilable string?))

(s/def ::reports any?)

(s/def ::db (s/keys :req [::loading?
                          ::runUUID
                          ::reports]))

(def defaults {::runUUID  nil
               ::reports nil})
