(ns sixsq.slipstream.webui.data.spec
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.utils.time :as time]
    [sixsq.slipstream.webui.data.utils :as utils]))


(s/def ::time-period (s/tuple any? any?))
(s/def ::time-period-filter (s/nilable string?))


(s/def ::service-offers any?)

(s/def ::credentials (s/nilable (s/coll-of any? :kind vector?)))
(s/def ::cloud-filter (s/nilable string?))


(s/def ::content-types (s/nilable (s/coll-of string? :kind vector?)))


(s/def ::gnss-filter string?)

(s/def ::content-type-filter (s/nilable string?))

(s/def ::application-select-visible? boolean?)

(s/def ::loading-applications? boolean?)

(s/def ::applications (s/nilable (s/coll-of any? :kind vector?)))

(s/def ::db (s/keys :req [::time-period
                          ::time-period-filter
                          ::service-offers
                          ::credentials
                          ::cloud-filter
                          ::content-types
                          ::application-select-visible?
                          ::loading-applications?
                          ::applications
                          ::gnss-filter
                          ::content-type-filter
                          ]))

(def default-time-period [(time/parse-iso8601 "2018-01-01T00:00:00.00Z")
                          (time/parse-iso8601 "2018-10-31T11:45:00.00Z")])

;; FIXME: Make default dates use current date.
(def defaults {
               ::time-period                 default-time-period
               ::time-period-filter          (utils/create-time-period-filter default-time-period)
               ::service-offers              nil
               ::credentials                 nil
               ::cloud-filter                nil
               ::content-types               nil
               ::application-select-visible? false
               ::loading-applications?       false
               ::applications                nil
               ::gnss-filter                 "(resource:type='DATA' and resource:bucket^='gnss')"
               ::content-type-filter         nil
               })
