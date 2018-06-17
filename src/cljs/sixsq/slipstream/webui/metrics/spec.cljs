(ns sixsq.slipstream.webui.metrics.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)


(s/def ::raw-metrics (s/nilable map?))


(s/def ::jvm-threads (s/nilable vector?))


(s/def ::jvm-memory (s/nilable vector?))


(s/def ::ring-request-rates (s/nilable vector?))


(s/def ::ring-response-rates (s/nilable vector?))


(s/def ::loading-job-info? boolean?)


(s/def ::category string?)
(s/def ::value nat-int?)
(s/def ::job-stat (s/keys :req [::category ::value]))

(s/def ::success ::job-stat)
(s/def ::failed ::job-stat)
(s/def ::queued ::job-stat)
(s/def ::running ::job-stat)
(s/def ::total ::job-stat)
(s/def ::job-info (s/keys :req [::success
                                ::failed
                                ::queued
                                ::running
                                ::total]))


(s/def ::db (s/keys :req [::loading?
                          ::raw-metrics
                          ::jvm-threads
                          ::jvm-memory
                          ::ring-request-rates
                          ::ring-response-rates]))


(def defaults {::loading?            false
               ::raw-metrics         nil
               ::jvm-threads         nil
               ::jvm-memory          nil
               ::ring-request-rates  nil
               ::ring-response-rates nil
               ::loading-job-info?   false
               ::job-info            {::success {:category "success", :value 0}
                                      ::failed  {:category "failed", :value 0}
                                      ::queued  {:category "queued", :value 0}
                                      ::running {:category "running", :value 0}
                                      ::total   {:category "total", :value 0}}})
