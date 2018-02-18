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
               ::ring-response-rates nil})
