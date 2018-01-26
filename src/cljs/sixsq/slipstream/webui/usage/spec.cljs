(ns sixsq.slipstream.webui.usage.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::results any?)

(s/def ::loading-connectors-list? boolean?)

(s/def ::connectors-list vector?)

(s/def ::users-list vector?)

(s/def ::loading-users-list? boolean?)

(s/def ::selected-connectors vector?)

(s/def ::selected-user (s/nilable string?))

(s/def ::date-after any?)

(s/def ::date-before any?)

(s/def ::is-admin? boolean?)

(s/def ::db (s/keys :req [::loading?
                          ::results
                          ::loading-connectors-list?
                          ::selected-connectors
                          ::connectors-list
                          ::users-list
                          ::loading-users-list?
                          ::selected-user
                          ::date-after
                          ::date-before
                          ::is-admin?]))

(def defaults {::loading?                 false
               ::results                  nil
               ::loading-connectors-list? true
               ::loading-users-list?      true
               ::connectors-list          []
               ::selected-connectors      []
               ::selected-user            nil
               ::users-list               []
               ::date-after               nil
               ::date-before              nil
               ::is-admin?                false})
