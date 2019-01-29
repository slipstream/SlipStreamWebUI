(ns sixsq.slipstream.webui.application.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::completed? boolean?)

(s/def ::module-path (s/nilable string?))

(s/def ::module any?)

(s/def ::add-modal-visible? boolean?)

(s/def ::active-tab keyword?)

(s/def ::active-tab-application keyword?)

(s/def ::add-data (s/nilable map?))

(s/def ::add-modal-step keyword?)

(s/def ::db (s/keys :req [::completed?
                          ::module-path
                          ::module
                          ::add-modal-visible?
                          ::active-tab
                          ::add-data
                          ::add-modal-step
                          ::active-tab-application]))

(def defaults {::completed?             true
               ::module-path            nil
               ::module                 nil
               ::add-modal-visible?     false
               ::active-tab             :project
               ::add-data               nil
               ::add-modal-step         :select
               ::active-tab-application :general})
