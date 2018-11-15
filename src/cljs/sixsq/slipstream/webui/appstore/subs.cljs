(ns sixsq.slipstream.webui.appstore.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.appstore.spec :as appstore-spec]))


(reg-sub
  ::deployment-templates
  ::appstore-spec/deployment-templates)

(reg-sub
  ::elements-per-page
  ::appstore-spec/elements-per-page)


(reg-sub
  ::page
  ::appstore-spec/page)


(reg-sub
  ::deploy-modal-visible?
  ::appstore-spec/deploy-modal-visible?)


(reg-sub
  ::loading-deployment?
  ::appstore-spec/loading-deployment?)


(reg-sub
  ::deployment
  ::appstore-spec/deployment)


(reg-sub
  ::loading-credentials?
  ::appstore-spec/loading-credentials?)


(reg-sub
  ::selected-credential
  ::appstore-spec/selected-credential)


(reg-sub
  ::credentials
  ::appstore-spec/credentials)


(reg-sub
  ::step-id
  ::appstore-spec/step-id)


(reg-sub
  ::data-clouds
  (fn [db]
    (::appstore-spec/data-clouds db)))
