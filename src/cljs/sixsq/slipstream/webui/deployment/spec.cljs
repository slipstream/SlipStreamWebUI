(ns sixsq.slipstream.webui.deployment.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::offset string?)
(s/def ::limit string?)
(s/def ::cloud string?)
(s/def ::activeOnly int?)

(s/def ::query-params (only-keys :req-un [::offset ::limit ::cloud ::activeOnly]))

(s/def ::deployments any?)

(s/def ::deployments-creds-map any?)

(s/def ::page int?)
(s/def ::elements-per-page int?)

(s/def ::full-text-search (s/nilable string?))

(s/def ::db (s/keys :req [::loading?
                          ::query-params
                          ::deployments
                          ::page
                          ::elements-per-page
                          ::full-text-search
                          ::deployments-creds-map]))

(def defaults {::loading?              false
               ::page                  1
               ::elements-per-page     10
               ::full-text-search      nil
               ::query-params          {:offset     ""
                                        :limit      ""
                                        :cloud      ""
                                        :activeOnly 1}
               ::deployments           nil
               ::deployments-creds-map {}})
