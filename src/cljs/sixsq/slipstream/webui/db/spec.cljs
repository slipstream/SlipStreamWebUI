(ns sixsq.slipstream.webui.db.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.authn.spec :as authn]
    [sixsq.slipstream.webui.cimi.spec :as cimi]
    [sixsq.slipstream.webui.cimi-detail.spec :as cimi-detail]
    [sixsq.slipstream.webui.client.spec :as client]
    [sixsq.slipstream.webui.dashboard.spec :as dashboard]
    [sixsq.slipstream.webui.i18n.spec :as i18n]
    [sixsq.slipstream.webui.main.spec :as main]
    [sixsq.slipstream.webui.metrics.spec :as metrics]
    [sixsq.slipstream.webui.usage.spec :as usage]))


(s/def ::db (s/merge ::authn/db
                     ::cimi/db
                     ::cimi-detail/db
                     ::client/db
                     ::dashboard/db
                     ::i18n/db
                     ::main/db
                     ::metrics/db
                     ::usage/db))


(def default-db
  (merge authn/defaults
         cimi/defaults
         cimi-detail/defaults
         client/defaults
         dashboard/defaults
         i18n/defaults
         main/defaults
         metrics/defaults
         usage/defaults))
