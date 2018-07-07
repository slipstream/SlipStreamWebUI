(ns sixsq.slipstream.webui.authn.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.config :as config]))


(s/def ::open-modal (s/nilable #{:login :signup}))

(s/def ::selected-method-group (s/nilable any?))

(s/def ::session (s/nilable any?))

(s/def ::error-message (s/nilable string?))

(s/def ::redirect-uri (s/nilable string?))

(s/def ::server-redirect-uri string?)

(s/def ::form-id (s/nilable string?))

(s/def ::db (s/keys :req [::open-modal
                          ::selected-method-group
                          ::session
                          ::error-message
                          ::redirect-uri
                          ::server-redirect-uri
                          ::form-id]))

(def defaults
  {::open-modal            nil
   ::selected-method-group nil
   ::session               nil
   ::error-message         nil
   ::redirect-uri          nil
   ::server-redirect-uri   (str @config/path-prefix "/profile")
   ::form-id               nil})
