(ns sixsq.slipstream.webui.dashboard.spec
  (:require
    [clojure.spec.alpha :as s]))


(s/def ::loading? boolean?)

(s/def ::statistics any?)

(s/def ::selected-tab int?)

(s/def ::filtered-cloud (s/nilable string?))

(s/def ::page int?)

(s/def ::total-pages int?)

(s/def ::virtual-machines any?)

(s/def ::records-displayed int?)

(s/def ::db (s/keys :req [::loading? ::statistics]))

(def defaults {::loading?          false
               ::statistics        nil
               ::selected-tab      0
               ::filtered-cloud    nil
               ::virtual-machines  nil
               ::records-displayed 10
               ::page              1
               ::total-pages       0})
