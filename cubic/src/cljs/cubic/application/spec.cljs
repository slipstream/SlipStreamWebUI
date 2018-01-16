(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.application.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::completed? boolean?)

(s/def ::module-id (s/nilable string?))

(s/def ::module any?)

(s/def ::db (s/keys :req [::completed? ::module-id ::module]))

(def defaults {::completed? true
               ::module-id nil
               ::module nil})
