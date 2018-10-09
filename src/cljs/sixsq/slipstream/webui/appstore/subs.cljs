(ns sixsq.slipstream.webui.appstore.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.appstore.spec :as appstore-spec]))


(reg-sub
  ::modules
  ::appstore-spec/modules)


(reg-sub
  ::paths
  ::appstore-spec/paths)


(reg-sub
  ::parent-path-search
  ::appstore-spec/parent-path-search)

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
  ::loading-deployment-templates?
  ::appstore-spec/loading-deployment-templates?)


(reg-sub
  ::selected-deployment-template
  ::appstore-spec/selected-deployment-template)


(reg-sub
  ::deployment-templates
  ::appstore-spec/deployment-templates)

(reg-sub
  ::deploy-module
  ::appstore-spec/deploy-module)