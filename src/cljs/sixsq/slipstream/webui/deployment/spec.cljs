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

(s/def ::db (s/keys :req [::loading?
                          ::filter-visible?
                          ::query-params
                          ::deployments
                          ::deployment-target]))

(def defaults {::loading?          false
               ::filter-visible?   false
               ::query-params      {:offset     ""
                                    :limit      ""
                                    :cloud      ""
                                    :activeOnly 1}
               ::deployments       nil
               ::deployment-target nil})
