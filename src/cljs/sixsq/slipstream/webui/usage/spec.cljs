(ns sixsq.slipstream.webui.usage.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.usage.utils :as u]))


(s/def ::loading? boolean?)

(s/def ::filter-visible? boolean?)

(s/def ::results any?)

(s/def ::users-list vector?)

(s/def ::credentials-list any?)

(s/def ::loading-credentials-list? boolean?)

(s/def ::selected-credentials vector?)

(s/def ::loading-users-list? boolean?)

(s/def ::selected-user (s/nilable string?))

(s/def ::date-range (s/tuple any? any?))

(s/def ::is-admin? boolean?)

(s/def ::db (s/keys :req [::loading?
                          ::filter-visible?
                          ::results
                          ::loading-users-list?
                          ::credentials-list
                          ::loading-credentials-list?
                          ::users-list
                          ::selected-user
                          ::date-range
                          ::is-admin?]))

(def defaults {::loading?                  false
               ::filter-visible?           false
               ::results                   nil
               ::loading-users-list?       true
               ::users-list                []
               ::credentials-list          []
               ::loading-credentials-list? true
               ::selected-user             nil
               ::date-range                (u/default-date-range)
               ::is-admin?                 false})
