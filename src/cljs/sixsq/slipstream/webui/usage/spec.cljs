(ns sixsq.slipstream.webui.usage.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.usage.utils :as u]))


(s/def ::loading? boolean?)

(s/def ::filter-visible? boolean?)

(s/def ::results any?)

(s/def ::credentials-map any?)

(s/def ::selected-credentials vector?)

(s/def ::selected-users-roles (s/nilable string?))

(s/def ::loading-credentials-map? boolean?)

(s/def ::date-range (s/tuple any? any?))

(s/def ::is-admin? boolean?)

(s/def ::db (s/keys :req [::loading?
                          ::filter-visible?
                          ::results
                          ::credentials-map
                          ::selected-credentials
                          ::loading-credentials-map?
                          ::selected-users-roles
                          ::date-range
                          ::is-admin?]))

(def defaults {::loading?                 false
               ::filter-visible?          false
               ::results                  nil
               ::credentials-map          {}
               ::selected-credentials     []
               ::loading-credentials-map? true
               ::selected-users-roles     nil
               ::date-range               (get u/date-range-entries u/default-date-range)
               ::is-admin?                false})
