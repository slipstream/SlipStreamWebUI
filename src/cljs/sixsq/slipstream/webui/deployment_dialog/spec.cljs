(ns sixsq.slipstream.webui.deployment-dialog.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::deploy-modal-visible? boolean?)

(s/def ::loading-deployment? boolean?)
(s/def ::deployment any?)

(s/def ::loading-credentials? boolean?)
(s/def ::credentials (s/nilable (s/coll-of any? :kind vector?)))
(s/def ::selected-credential any?)

(s/def ::data-clouds any?)
(s/def ::selected-cloud (s/nilable string?))
(s/def ::cloud-filter (s/nilable string?))

(s/def ::connectors any?)

(s/def ::step-id #{:data :credentials :size :parameters :summary})

(def steps [:data :credentials :size :parameters :summary])

(s/def ::active-step ::step-id)

(s/def ::loading? boolean?)

(s/def ::completed? boolean?)

(s/def ::icon string?)

(s/def ::summary any?)

(s/def ::data any?)

(s/def ::step-state (s/keys :req-un [::step-id
                                     ::loading?
                                     ::completed?
                                     ::icon
                                     ::summary
                                     ::data]))

(s/def ::step-states (s/map-of ::step-id ::step-state))


(s/def ::db (s/keys :req [::deploy-modal-visible?
                          ::loading-deployment?
                          ::deployment
                          ::loading-credentials?
                          ::credentials
                          ::selected-credential
                          ::data-clouds
                          ::selected-cloud
                          ::cloud-filter
                          ::connectors

                          ::active-step
                          ::step-states]))


(def defaults {::deploy-modal-visible? false
               ::loading-deployment?   false
               ::deployment            nil
               ::loading-credentials?  false
               ::credentials           nil
               ::selected-credential   nil
               ::data-clouds           nil
               ::selected-cloud        nil
               ::cloud-filter          nil
               ::connectors            nil

               ::active-step           :data
               ::step-states           {:data        {:step-id    :data
                                                      :loading?   false
                                                      :completed? false
                                                      :icon       "database"
                                                      :summary    nil
                                                      :data       nil}
                                        :credentials {:step-id    :credentials
                                                      :loading?   false
                                                      :completed? false
                                                      :icon       "key"
                                                      :summary    nil
                                                      :data       nil}
                                        :size        {:step-id    :size
                                                      :loading?   false
                                                      :completed? false
                                                      :icon       "expand arrows alternate"
                                                      :summary    nil
                                                      :data       nil}
                                        :parameters  {:step-id    :parameters
                                                      :loading?   false
                                                      :completed? false
                                                      :icon       "list alternate outline"
                                                      :summary    nil
                                                      :data       nil}
                                        :summary     {:step-id    :summary
                                                      :loading?   false
                                                      :completed? false
                                                      :icon       "info"
                                                      :summary    nil
                                                      :data       nil}}})
