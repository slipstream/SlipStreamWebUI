(ns sixsq.slipstream.webui.authn.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.config :as config]
    [taoensso.timbre :as log]))


(s/def ::open-modal (s/nilable any?))

(s/def ::session (s/nilable any?))

(s/def ::error-message (s/nilable string?))

(s/def ::total nat-int?)

(s/def ::count nat-int?)

(s/def ::redirect-uri string?)

(s/def ::server-redirect-uri string?)

(s/def ::db (s/keys :req [::open-modal ::session ::error-message ::forms ::redirect-uri]))

(def defaults
  {::open-modal          nil
   ::session             nil
   ::error-message       nil
   ::server-redirect-uri (str @config/path-prefix "/profile")
   ::redirect-uri        nil})
