(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.deployment.spec
  (:require-macros [cubic.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::offset string?)
(s/def ::limit string?)
(s/def ::cloud string?)
(s/def ::activeOnly int?)

(s/def ::query-params (only-keys :req-un [::offset ::limit ::cloud ::activeOnly]))

(s/def ::deployments any?)

(s/def ::db (s/keys :req [::loading? ::query-params ::deployments]))

(def defaults {::loading?     false
               ::query-params [:offset ""
                               :limit ""
                               :cloud ""
                               :activeOnly int?]
               ::deployments  nil})
