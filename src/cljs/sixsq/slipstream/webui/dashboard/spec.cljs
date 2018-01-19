(ns sixsq.slipstream.webui.dashboard.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::statistics any?)

(s/def ::db (s/keys :req [::loading? ::statistics]))

(def defaults {::loading? false
               ::statistics nil})
