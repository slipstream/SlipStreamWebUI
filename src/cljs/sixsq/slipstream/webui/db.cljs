(ns sixsq.slipstream.webui.db
  (:require
    [cljs.spec :as s]))

;;
;; define schema of the local database
;;

(s/def ::client any?)

(s/def ::message (s/nilable string?))

(s/def ::logged-in? boolean?)
(s/def ::user-id (s/nilable string?))
(s/def ::authn (s/keys :req-un [::logged-in? ::user-id]))

(s/def ::id string?)
(s/def ::resourceURI string?)
(s/def ::created string?)
(s/def ::updated string?)


(s/def ::type #{"ROLE" "USER"})
(s/def ::principal string?)
(s/def ::right #{"ALL" "VIEW" "RUN" "CREATE"})
(s/def ::rule (s/keys :req-un [::principal ::type ::right]))
(s/def ::rules (s/coll-of ::rule))
(s/def ::owner (s/keys :req-un [::principal ::type]))
(s/def ::acl (s/keys :req-un [::owner ::rules]))

(s/def ::baseURI string?)

(s/def ::cloud-entry-point (s/nilable (s/keys :req-un [::id ::created ::updated ::acl ::baseURI])))

;;(s/def ::cloud-entry-point (s/nilable string?))

(s/def ::search-resource (s/nilable string?))

(s/def ::results (s/nilable string?))

(s/def ::db (s/keys :req-un [::client ::message ::authn ::cloud-entry-point ::search-resource ::results]))

;;
;; initial database value
;;

(def default-value
  {:client            nil
   :message           nil
   :authn             {:logged-in? false
                       :user-id    nil}
   :cloud-entry-point nil
   :search-resource   nil
   :results           nil})
