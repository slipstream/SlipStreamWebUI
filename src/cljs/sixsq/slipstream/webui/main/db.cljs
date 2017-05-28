(ns sixsq.slipstream.webui.main.db
  (:require
    [clojure.spec.alpha :as s]
    [re-frame.core :refer [after]]
    [sixsq.slipstream.webui.widget.i18n.dictionary :as dictionary]))

;;
;; check schema after every change
;;
(defn check-and-throw
  "throw an exception if db doesn't match the spec."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; check that all state changes from event handlers are correct
(def check-spec-interceptor (after (partial check-and-throw :sixsq.slipstream.webui.main.db/db)))

;;
;; define schema of the local database
;;

(s/def ::cimi any?)
(s/def ::runs any?)
(s/def ::modules any?)

(s/def ::clients (s/nilable (s/keys :req-un [::cimi ::runs ::modules])))

(s/def ::message (s/nilable string?))

(s/def ::resource-data (s/nilable any?))

(s/def ::runs-data (s/nilable any?))

(s/def ::offset string?)
(s/def ::limit string?)
(s/def ::cloud (s/nilable string?))
(s/def ::activeOnly int?)
(s/def ::runs-params (s/keys :req-un [::offset ::limit ::cloud ::activeOnly]))

(s/def ::modules-data (s/nilable any?))
(s/def ::modules-path (s/nilable string?))
(s/def ::modules-breadcrumbs (s/nilable (s/coll-of string?)))

;;
;; authentication state
;;

(s/def :webui.authn/session (s/nilable (s/map-of keyword? any?)))

(s/def :webui.authn/method (s/nilable string?))

(s/def :webui.authn/id string?)
(s/def :webui.authn/label string?)
(s/def :webui.authn/description string?)
(s/def :webui.authn/params-desc (s/map-of keyword? map?))
(s/def :webui.authn/method-defn (s/merge (s/keys :req-un [:webui.authn/id
                                                          :webui.authn/label
                                                          :webui.authn/description
                                                          :webui.authn/params-desc])
                                         (s/map-of #{:id :label :description :params-desc} any?)))
(s/def :webui.authn/methods (s/coll-of :webui.authn/method-defn))
(s/def :webui.authn/forms (s/map-of string? map?))

(s/def :webui.authn/authn (s/keys :req-un [:webui.authn/session
                                           :webui.authn/method
                                           :webui.authn/methods
                                           :webui.authn/forms]))

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

(s/def ::locale string?)
(s/def ::tr fn?)
(s/def ::i18n (s/keys :req-un [::locale ::tr]))

(s/def ::search (s/keys :req-un [::collection-name ::params ::results ::completed?
                                 ::available-fields ::selected-fields]))

(s/def ::offer-data (s/nilable any?))
(s/def ::offer ::search)

(s/def ::resource-path (s/coll-of string?))

(s/def ::db (s/merge
              (s/keys :req-un [::i18n
                               ::clients ::message ::resource-data ::resource-path
                               ::runs-data ::runs-params
                               ::modules-data ::modules-path ::modules-breadcrumbs
                               :webui.authn/authn ::cloud-entry-point ::search
                               ::offer-data ::offer])
              (s/map-of #{:i18n
                          :clients :message :resource-data :resource-path
                          :runs-data :runs-params
                          :modules-data :modules-path :modules-breadcrumbs
                          :authn :cloud-entry-point :search
                          :offer-data :offer} any?)))

;;
;; initial database value
;;

(def default-value
  {:i18n                {:locale "en"
                         :tr     (dictionary/create-tr-fn "en")}

   :clients             nil
   :message             nil
   :resource-data       nil
   :resource-path       []

   :runs-data           nil
   :runs-params         {:offset     "0"
                         :limit      "10"
                         :cloud      nil
                         :activeOnly 0}

   :modules-data        nil
   :modules-path        nil
   :modules-breadcrumbs nil

   :authn               {:session      nil
                         :method       nil
                         :methods      []
                         :forms        {}}

   :cloud-entry-point   nil
   :search              {:collection-name  nil
                         :params           {:$first  1
                                            :$last   20
                                            :$filter nil}
                         :results          nil
                         :completed?       true
                         :available-fields [{:id "id" :label "id"}
                                            {:id "beta" :label "beta"}]
                         :selected-fields  #{"id"}}
   :offer-data          nil
   :offer               {:collection-name  "serviceOffers"
                         :params           {:$first  1
                                            :$last   20
                                            :$filter nil}
                         :results          nil
                         :completed?       true
                         :available-fields [{:id "id" :label "id"}
                                            {:id "beta" :label "beta"}]
                         :selected-fields  #{"id"}}})
