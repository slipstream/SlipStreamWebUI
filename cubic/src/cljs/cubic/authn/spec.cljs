(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.authn.spec
  (:require-macros [cubic.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [cubic.config :as config]
    [taoensso.timbre :as log]))


(s/def ::modal-open? boolean?)

(s/def ::session (s/nilable any?))

(s/def ::error-message (s/nilable string?))

(s/def ::total nat-int?)

(s/def ::count nat-int?)

(s/def :method/id string?)
(s/def :method/label string?)
(s/def :method/group (s/nilable string?))
(s/def :method/authn-method string?)
(s/def :method/description string?)
(s/def :method/params-desc (s/map-of keyword? map?))
(s/def :method/method-defn (only-keys :req-un [:method/id
                                               :method/label
                                               :method/group
                                               :method/authn-method
                                               :method/description
                                               :method/params-desc]))

(s/def ::redirect-uri string?)

(s/def ::server-redirect-uri string?)

(s/def ::methods (s/coll-of :method/method-defn :kind vector?))

(s/def ::db (s/keys :req [::modal-open? ::session ::error-message ::total ::count ::methods ::forms ::redirect-uri]))

(def defaults
  {::modal-open?         false
   ::session             nil
   ::error-message       nil
   ::total               0
   ::count               0
   ::methods             []
   ::server-redirect-uri (str @config/path-prefix "/profile")
   ::redirect-uri        nil})
