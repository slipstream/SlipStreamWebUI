(ns sixsq.slipstream.webui.appstore.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::modules any?)

(s/def ::full-text-search (s/nilable string?))

(s/def ::parent-path-search (s/nilable string?))

(s/def ::paths (s/coll-of string? :kind vector?))

(s/def ::page int?)
(s/def ::elements-per-page int?)
(s/def ::total-elements int?)

(s/def ::deploy-modal-visible? boolean?)
(s/def ::deploy-module any?)

(s/def ::loading-deployment-templates? boolean?)
(s/def ::selected-deployment-template any?)
(s/def ::deployment-templates (s/nilable (s/coll-of any? :kind vector?)))

(s/def ::loading-credentials? boolean?)
(s/def ::selected-credential any?)
(s/def ::credentials (s/nilable (s/coll-of any? :kind vector?)))

(s/def ::db (s/keys :req [::modules
                          ::full-text-search
                          ::parent-path-search
                          ::paths
                          ::page
                          ::elements-per-page
                          ::total-elements
                          ::deploy-module
                          ::deploy-modal-visible?
                          ::loading-deployment-templates?
                          ::selected-deployment-template
                          ::deployment-templates
                          ::loading-credentials?
                          ::selected-credential
                          ::credentials]))

(def defaults {::modules                       nil
               ::paths                         []
               ::full-text-search              nil
               ::parent-path-search            ""
               ::page                          1
               ::elements-per-page             8
               ::total-elements                0
               ::deploy-module                 nil
               ::deploy-modal-visible?         false
               ::loading-deployment-templates? false
               ::selected-deployment-template  nil
               ::deployment-templates          nil
               ::loading-credentials?          false
               ::selected-credential           nil
               ::credentials                   nil})
