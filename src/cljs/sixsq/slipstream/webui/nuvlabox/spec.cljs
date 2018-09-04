(ns sixsq.slipstream.webui.nuvlabox.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::stale-count nat-int?)
(s/def ::active-count nat-int?)
(s/def ::healthy? (s/map-of string? boolean?))
(s/def ::health-info (s/keys :req-un [::stale-count
                                      ::active-count
                                      ::healthy?]))


(s/def ::$first nat-int?)
(s/def ::$last nat-int?)
(s/def ::$filter (s/nilable string?))
(s/def ::$orderby (s/nilable string?))
(s/def ::$select (s/nilable string?))
(s/def ::$aggregation (s/nilable string?))

(s/def ::query-params (s/keys :req-un [::$first
                                       ::$last
                                       ::$filter
                                       ::$orderby
                                       ::$select
                                       ::$aggregation]))

(s/def ::collection any?)

(s/def ::state-selector #{"all" "new" "activated" "quarantined"})


(s/def ::db (s/keys :req [::loading?
                          ::health-info

                          ::query-params

                          ::collection
                          ::state-selector]))


(def defaults {::loading?                    false
               ::health-info                 {:stale-count  0
                                              :active-count 0
                                              :healthy?     {}}

               ::query-params                {:$first       0
                                              :$last        20
                                              :$filter      nil
                                              :$orderby     nil
                                              :$select      nil
                                              :$aggregation nil}

               ::collection                  nil
               ::state-selector              "all"})

