(ns sixsq.slipstream.webui.db.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.slipstream.webui.application.spec :as application]
    [sixsq.slipstream.webui.authn.spec :as authn]
    [sixsq.slipstream.webui.cimi-detail.spec :as cimi-detail]
    [sixsq.slipstream.webui.cimi.spec :as cimi]
    [sixsq.slipstream.webui.client.spec :as client]
    [sixsq.slipstream.webui.dashboard.spec :as dashboard]
    [sixsq.slipstream.webui.deployment.spec :as deployment]
    [sixsq.slipstream.webui.i18n.spec :as i18n]
    [sixsq.slipstream.webui.legacy-application.spec :as legacy-application]
    [sixsq.slipstream.webui.main.spec :as main]
    [sixsq.slipstream.webui.messages.spec :as messages]
    [sixsq.slipstream.webui.metrics.spec :as metrics]
    [sixsq.slipstream.webui.usage.spec :as usage]))


(s/def ::db (s/merge ::application/db
                     ::authn/db
                     ::cimi/db
                     ::cimi-detail/db
                     ::client/db
                     ::dashboard/db
                     ::i18n/db
                     ::legacy-application/db
                     ::main/db
                     ::metrics/db
                     ::messages/db
                     ::usage/db))


(def default-db
  (merge application/defaults
         authn/defaults
         cimi/defaults
         cimi-detail/defaults
         deployment/defaults
         client/defaults
         dashboard/defaults
         i18n/defaults
         legacy-application/defaults
         main/defaults
         metrics/defaults
         messages/defaults
         usage/defaults))
