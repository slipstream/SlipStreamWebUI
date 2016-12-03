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

(s/def ::search-resource (s/nilable string?))

(s/def ::search-results (s/nilable any?))

(s/def ::cimi-filter (s/nilable string?))
(s/def ::page nat-int?)
(s/def ::page-size (s/int-in 1 50))

(s/def ::webui (s/keys :req-un [::cimi-filter ::page ::page-size]))

(s/def ::$first nat-int?)
(s/def ::$last nat-int?)
(s/def ::$filter (s/nilable string?))

(s/def ::params (s/keys :req-un [::$first ::$last ::$filter]))

(s/def ::collection-name (s/nilable string?))
(s/def ::results (s/nilable any?))
(s/def ::completed? boolean?)

(s/def ::label string?)
(s/def ::choice (s/keys :req-un [::id ::label]))
(s/def ::available-fields (s/coll-of ::choice))
(s/def ::selected-fields (s/coll-of ::id))

(s/def ::search (s/keys :req-un [::collection-name ::params ::results ::completed?
                                 ::available-fields ::selected-fields]))

(s/def ::db (s/keys :req-un [::client ::message ::authn ::cloud-entry-point ::search]))

;;
;; initial database value
;;

(def default-value
  {:client            nil
   :message           nil
   :authn             {:logged-in? false
                       :user-id    nil}
   :cloud-entry-point nil
   :search            {:collection-name  nil
                       :params           {:$first  1
                                          :$last   20
                                          :$filter nil}
                       :results          nil
                       :completed?       true
                       :available-fields [{:id "id" :label "id"}
                                          {:id "beta" :label "beta"}]
                       :selected-fields #{"id"}}})
