(ns sixsq.slipstream.webui.data.spec
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.utils.time :as time]))


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




(s/def ::time-period (s/tuple any? any?))
(s/def ::service-offers any?)
(s/def ::credentials (s/nilable (s/coll-of any? :kind vector?)))
(s/def ::content-types (s/nilable (s/coll-of string? :kind vector?)))


(s/def ::db (s/keys :req [

                          ::time-period
                          ::service-offers
                          ::credentials
                          ::content-types

                          ::deployment-templates
                          ::full-text-search
                          ::page
                          ::elements-per-page
                          ::deployment
                          ::deploy-modal-visible?
                          ::loading-deployment?
                          ::loading-credentials?
                          ::selected-credential
                          ::step-id]))

(def defaults {
               ::time-period           [(time/days-before 1) (time/now)]
               ::service-offers        nil
               ::credentials           nil
               ::content-types         nil

               ::deployment-templates  nil
               ::full-text-search      nil
               ::page                  1
               ::elements-per-page     8
               ::deployment            nil
               ::deploy-modal-visible? false
               ::loading-deployment?   false
               ::loading-credentials?  false
               ::selected-credential   nil
               ::step-id               "summary"})
