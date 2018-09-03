(ns sixsq.slipstream.webui.quota.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::loading-quotas? boolean?)

(s/def ::credentials-quotas-map coll?)

(s/def ::db (s/keys :req [::loading?
                          ::loading-quotas?
                          ::credentials-quotas-map]))

(def defaults {::loading?               false
               ::loading-quotas?        true
               ::credentials-quotas-map []})
