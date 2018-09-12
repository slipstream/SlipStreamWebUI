(ns sixsq.slipstream.webui.application.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::completed? boolean?)

(s/def ::module-path (s/nilable string?))

(s/def ::module any?)

(s/def ::add-modal-visible? boolean?)

(s/def ::active-tab keyword?)

(s/def ::add-data (s/nilable map?))

(s/def ::deploy-modal-visible? boolean?)

(s/def ::loading-deployment-templates? boolean?)

(s/def ::selected-deployment-template any?)

(s/def ::deployment-templates any?)

(s/def ::db (s/keys :req [::completed?
                          ::module-path
                          ::module
                          ::add-modal-visible?
                          ::active-tab
                          ::add-data
                          ::deploy-modal-visible?
                          ::loading-deployment-templates?
                          ::selected-deployment-template
                          ::deployment-templates]))

(def defaults {::completed?                    true
               ::module-path                   nil
               ::module                        nil
               ::add-modal-visible?            false
               ::active-tab                    :project
               ::add-data                      nil
               ::deploy-modal-visible?         false
               ::loading-deployment-templates? false
               ::selected-deployment-template  nil
               ::deployment-templates          nil})
