(ns sixsq.slipstream.webui.main.spec
  (:require-macros [sixsq.slipstream.webui.utils.spec :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::sidebar-open? boolean?)

(s/def ::visible? boolean?)

(s/def ::nav-path any?)

(s/def ::nav-query-params any?)

(s/def ::db (s/keys :req [::sidebar-open?
                          ::visible?
                          ::nav-path
                          ::nav-query-params]))

(def defaults {::sidebar-open?    true
               ::visible?         true
               ::nav-path         ["cimi"]
               ::nav-query-params {}})
