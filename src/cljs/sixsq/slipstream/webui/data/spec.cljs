(ns sixsq.slipstream.webui.data.spec
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.data.utils :as utils]
    [sixsq.slipstream.webui.utils.time :as time]))


(s/def ::time-period (s/tuple any? any?))
(s/def ::time-period-filter (s/nilable string?))


(s/def ::service-offers any?)

(s/def ::credentials (s/nilable (s/coll-of any? :kind vector?)))

(s/def ::cloud-filter (s/nilable string?))

(s/def ::content-type-filter (s/nilable string?))

(s/def ::application-select-visible? boolean?)

(s/def ::loading-applications? boolean?)

(s/def ::applications (s/nilable (s/coll-of any? :kind vector?)))

(s/def ::data-queries any?)

(s/def ::full-text-search (s/nilable string?))

(s/def ::data any?)

(s/def ::db (s/keys :req [::time-period
                          ::time-period-filter
                          ::service-offers
                          ::credentials
                          ::cloud-filter
                          ::application-select-visible?
                          ::loading-applications?
                          ::applications
                          ::content-type-filter
                          ::data-queries
                          ::full-text-search
                          ::data
                          ]))

(def default-time-period [(time/parse-iso8601 "2018-10-01T00:00:00.00Z")
                          (time/parse-iso8601 "2018-11-15T00:00:00.00Z")])

(def data-queries
  {"data-query/feCapture-ionMessage" {:id                "data-query/feCapture-ionMessage"
                                      :name              "feCapture & ionMessage"
                                      :description       "Query that selects data and metadata for signals coming from the frontend receiver."
                                      :query-data        "resource:type='DATA' and resource:bucket^='gnss' and (data:contentType='application/x-ionMessage' or data:contentType='application/x-feCapture')"
                                      :query-application "dataAcceptContentTypes='application/x-ionMessage' and dataAcceptContentTypes='application/x-feCapture'"}

   "data-query/feCapture"            {:id                "data-query/feCapture"
                                      :name              "feCapture"
                                      :description       "Query that selects data feCapture only."
                                      :query-data        "resource:type='DATA' and resource:bucket^='gnss' and data:contentType='application/x-feCapture'"
                                      :query-application "dataAcceptContentTypes='application/x-feCapture'"}

   "data-query/ionMessage"           {:id                "data-query/ionMessage"
                                      :name              "ionMessage"
                                      :description       "Query that selects data ionMessage only."
                                      :query-data        "resource:type='DATA' and resource:bucket^='gnss' and data:contentType='application/x-ionMessage'"
                                      :query-application "dataAcceptContentTypes='application/x-ionMessage'"}

   "data-query/sdrData"              {:id                "data-query/sdrData"
                                      :name              "sdrData"
                                      :description       "Query that selects data sdrData only."
                                      :query-data        "resource:type='DATA' and resource:bucket^='gnss' and data:contentType='application/x-sdrData'"
                                      :query-application "dataAcceptContentTypes='application/x-sdrData'"}})

;; FIXME: Make default dates use current date.
(def defaults {::time-period                 default-time-period
               ::time-period-filter          (utils/create-time-period-filter default-time-period)
               ::service-offers              nil
               ::credentials                 nil
               ::cloud-filter                nil
               ::application-select-visible? false
               ::loading-applications?       false
               ::applications                nil
               ::content-type-filter         nil
               ::data-queries                data-queries
               ::full-text-search            nil
               ::data                        nil
               })
