(ns sixsq.slipstream.webui.docs-detail.spec
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::runUUID (s/nilable string?))

(s/def ::reports any?)

(s/def ::loading? boolean?)

(s/def ::cached-resource-id (s/nilable string?))

(s/def ::resource any?)

(s/def ::events any?)



(s/def ::db (s/keys :req [::runUUID
                          ::reports
                          ::loading?
                          ::cached-resource-id
                          ::resource
                          ::events]))


(def defaults {::runUUID            nil
               ::reports            nil
               ::loading?           false
               ::cached-resource-id nil
               ::resource           nil
               ::events             nil})
