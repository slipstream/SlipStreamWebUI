(ns sixsq.slipstream.webui.application.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::completed? boolean?)

(s/def ::module-id (s/nilable string?))

(s/def ::module any?)

(s/def ::add-modal-visible? boolean?)

(s/def ::active-tab keyword?)

(s/def ::add-data (s/nilable map?))

(s/def ::deploy-modal-visible? boolean?)

(s/def ::db (s/keys :req [::completed?
                          ::module-id
                          ::module
                          ::add-modal-visible?
                          ::active-tab
                          ::add-data
                          ::deploy-modal-visible?]))

(def defaults {::completed?            true
               ::module-id             nil
               ::module                nil
               ::add-modal-visible?    false
               ::active-tab            :project
               ::add-data              nil
               ::deploy-modal-visible? false})
