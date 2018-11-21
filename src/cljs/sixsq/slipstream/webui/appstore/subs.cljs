(ns sixsq.slipstream.webui.appstore.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.appstore.spec :as spec]))


(reg-sub
  ::deployment-templates
  ::spec/deployment-templates)

(reg-sub
  ::elements-per-page
  ::spec/elements-per-page)


(reg-sub
  ::page
  ::spec/page)


(reg-sub
  ::deploy-modal-visible?
  ::spec/deploy-modal-visible?)


(reg-sub
  ::loading-deployment?
  ::spec/loading-deployment?)


(reg-sub
  ::deployment
  ::spec/deployment)


(reg-sub
  ::loading-credentials?
  ::spec/loading-credentials?)


(reg-sub
  ::selected-credential
  ::spec/selected-credential)


(reg-sub
  ::credentials
  ::spec/credentials)


(reg-sub
  ::step-id
  ::spec/step-id)


(reg-sub
  ::data-clouds
  (fn [db]
    (::spec/data-clouds db)))


(reg-sub
  ::selected-cloud
  (fn [db]
    (::spec/selected-cloud db)))

(reg-sub
  ::connectors
  (fn [db]
    (::spec/connectors db)))
