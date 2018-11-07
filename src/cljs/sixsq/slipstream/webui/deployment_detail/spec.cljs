(ns sixsq.slipstream.webui.deployment-detail.spec
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::reports any?)

(s/def ::loading? boolean?)

(s/def ::deployment any?)

(s/def ::global-deployment-parameters any?)

(s/def ::events any?)

(s/def ::node-parameters-modal (s/nilable string?))

(s/def ::node-parameters any?)

(s/def ::summary-nodes-parameters any?)


(s/def ::db (s/keys :req [::reports
                          ::loading?
                          ::deployment
                          ::global-deployment-parameters
                          ::events
                          ::node-parameters-modal
                          ::node-parameters
                          ::summary-nodes-parameters]))


(def defaults {::reports                      nil
               ::loading?                     false
               ::deployment                   nil
               ::global-deployment-parameters nil
               ::events                       nil
               ::node-parameters-modal        nil
               ::node-parameters              nil
               ::summary-nodes-parameters     nil})
