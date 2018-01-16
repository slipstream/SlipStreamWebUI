(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.i18n.spec
  (:require
    [clojure.spec.alpha :as s]
    [cubic.i18n.utils :as utils]))

(s/def ::locale string?)

(s/def ::tr fn?)

(s/def ::db (s/keys :req [::locale ::tr]))

(def defaults {::locale "en"
               ::tr     (utils/create-tr-fn "en")})
