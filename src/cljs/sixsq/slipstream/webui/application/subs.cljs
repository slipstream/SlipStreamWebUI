(ns sixsq.slipstream.webui.application.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.application.spec :as application-spec]))


(reg-sub
  ::completed?
  ::application-spec/completed?)


(reg-sub
  ::module
  ::application-spec/module)


(reg-sub
  ::add-modal-visible?
  ::application-spec/add-modal-visible?)


(reg-sub
  ::add-data
  ::application-spec/add-data)


(reg-sub
  ::active-tab
  ::application-spec/active-tab)


(reg-sub
  ::deploy-modal-visible?
  ::application-spec/deploy-modal-visible?)


(reg-sub
  ::loading-deployment-templates?
  ::application-spec/loading-deployment-templates?)


(reg-sub
  ::selected-deployment-template
  ::application-spec/selected-deployment-template)


(reg-sub
  ::deployment-templates
  ::application-spec/deployment-templates)
