(ns sixsq.slipstream.webui.deployment.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::filter-visible? boolean?)

(s/def ::offset string?)
(s/def ::limit string?)
(s/def ::cloud string?)
(s/def ::activeOnly int?)

(s/def ::query-params (only-keys :req-un [::offset ::limit ::cloud ::activeOnly]))

(s/def ::deployments any?)

(s/def ::deployment-target (s/nilable string?))

(s/def ::user-connectors-loading? boolean?)

(s/def ::user-connectors (s/nilable (s/coll-of string? :kind vector?)))

(s/def ::place-and-rank-loading? boolean?)

(s/def ::place-and-rank (s/nilable any?))

(s/def ::db (s/keys :req [::loading?
                          ::filter-visible?
                          ::query-params
                          ::deployments
                          ::deployment-target
                          ::user-connectors-loading?
                          ::user-connectors
                          ::place-and-rank-loading?
                          ::place-and-rank]))

(def defaults {::loading?                 false
               ::filter-visible?          false
               ::query-params             {:offset     ""
                                           :limit      ""
                                           :cloud      ""
                                           :activeOnly 1}
               ::deployments              nil
               ::deployment-target        nil
               ::user-connectors-loading? false
               ::user-connectors          nil
               ::place-and-rank-loading?  false
               ::place-and-rank           nil})
