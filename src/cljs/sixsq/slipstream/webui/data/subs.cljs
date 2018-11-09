(ns sixsq.slipstream.webui.data.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.data.spec :as spec]))


(reg-sub
  ::time-period
  (fn [db]
    (::spec/time-period db)))


(reg-sub
  ::service-offers
  (fn [db]
    (::spec/service-offers db)))


(reg-sub
  ::credentials
  (fn [db]
    (::spec/credentials db)))


(reg-sub
  ::content-types
  (fn [db]
    (::spec/content-types db)))



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
  ::step-id
  ::spec/step-id)
