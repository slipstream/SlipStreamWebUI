(ns sixsq.slipstream.webui.deployment-dialog.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::deploy-modal-visible? boolean?)

(s/def ::loading-deployment? boolean?)
(s/def ::deployment any?)

(s/def ::loading-credentials? boolean?)
(s/def ::credentials (s/nilable (s/coll-of any? :kind vector?)))
(s/def ::selected-credential any?)

(s/def ::step-id string?)

(s/def ::data-clouds any?)
(s/def ::selected-cloud (s/nilable string?))
(s/def ::cloud-filter (s/nilable string?))

(s/def ::connectors any?)


(s/def ::db (s/keys :req [::deploy-modal-visible?
                          ::loading-deployment?
                          ::deployment
                          ::loading-credentials?
                          ::credentials
                          ::selected-credential
                          ::step-id
                          ::data-clouds
                          ::selected-cloud
                          ::cloud-filter
                          ::connectors]))


(def defaults {::deploy-modal-visible? false
               ::loading-deployment?   false
               ::deployment            nil
               ::loading-credentials?  false
               ::credentials           nil
               ::selected-credential   nil
               ::step-id               "data"
               ::data-clouds           nil
               ::selected-cloud        nil
               ::cloud-filter          nil
               ::connectors            nil})
