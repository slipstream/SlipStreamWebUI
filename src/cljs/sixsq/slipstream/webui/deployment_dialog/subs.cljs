(ns sixsq.slipstream.webui.deployment-dialog.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.deployment-dialog.spec :as spec]))


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
  ::active-step
  (fn [db]
    (::spec/active-step db)))


(reg-sub
  ::step-states
  (fn [db]
    (::spec/step-states db)))


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
