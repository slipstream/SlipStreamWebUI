(ns sixsq.slipstream.webui.appstore.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::deployment-templates any?)

(s/def ::full-text-search (s/nilable string?))

(s/def ::page int?)
(s/def ::elements-per-page int?)

(s/def ::deploy-modal-visible? boolean?)
(s/def ::deployment any?)

(s/def ::loading-deployment? boolean?)
(s/def ::deployment-templates (s/nilable (s/coll-of any? :kind vector?)))

(s/def ::loading-credentials? boolean?)
(s/def ::selected-credential any?)
(s/def ::credentials (s/nilable (s/coll-of any? :kind vector?)))

(s/def ::step-id string?)

(s/def ::data-clouds any?)

(s/def ::cloud-filter (s/nilable string?))

(s/def ::db (s/keys :req [::deployment-templates
                          ::full-text-search
                          ::page
                          ::elements-per-page
                          ::deployment
                          ::deploy-modal-visible?
                          ::loading-deployment?
                          ::loading-credentials?
                          ::selected-credential
                          ::credentials
                          ::step-id
                          ::data-clouds
                          ::cloud-filter]))

(def defaults {::deployment-templates  nil
               ::full-text-search      nil
               ::page                  1
               ::elements-per-page     8
               ::deployment            nil
               ::deploy-modal-visible? false
               ::loading-deployment?   false
               ::loading-credentials?  false
               ::selected-credential   nil
               ::credentials           nil
               ::step-id               "summary"
               ::data-clouds           nil
               ::cloud-filter          nil})
