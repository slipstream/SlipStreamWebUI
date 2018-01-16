(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.dashboard.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::statistics any?)

(s/def ::db (s/keys :req [::loading? ::statistics]))

(def defaults {::loading? false
               ::statistics nil})
