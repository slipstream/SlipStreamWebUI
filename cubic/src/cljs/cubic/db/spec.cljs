(ns cubic.db.spec
  (:require-macros [cubic.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [cubic.authn.spec :as authn]
    [cubic.cimi.spec :as cimi]
    [cubic.cimi-detail.spec :as cimi-detail]
    [cubic.client.spec :as client]
    [cubic.dashboard.spec :as dashboard]
    [cubic.i18n.spec :as i18n]
    [cubic.main.spec :as main]))


(s/def ::db (s/merge ::authn/db
                     ::cimi/db
                     ::cimi-detail/db
                     ::client/db
                     ::dashboard/db
                     ::i18n/db
                     ::main/db))


(def default-db
  (merge authn/defaults
         cimi/defaults
         cimi-detail/defaults
         client/defaults
         dashboard/defaults
         i18n/defaults
         main/defaults))
