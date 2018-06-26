(ns sixsq.slipstream.webui.nuvlabox.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::stale (s/nilable any?))
(s/def ::active (s/nilable vector?))
(s/def ::state-info (s/keys :req-un [::stale ::active]))

(s/def ::detail-loading? boolean?)
(s/def ::mac (s/nilable string?))
(s/def ::detail (s/nilable any?))


(s/def ::db (s/keys :req [::loading?
                          ::state-info
                          ::detail-loading?
                          ::mac
                          ::detail]))


(def defaults {::loading?       false
               ::state-info     {:stale nil, :active nil}
               ::detail-loading false
               ::mac            nil
               ::detail         nil})
