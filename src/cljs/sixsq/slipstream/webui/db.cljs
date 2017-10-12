(ns sixsq.slipstream.webui.db
  (:require-macros [sixsq.slipstream.webui.db :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [re-frame.core :refer [after]]
    [expound.alpha :as expound]
    [sixsq.slipstream.webui.widget.i18n.dictionary :as dictionary]
    [taoensso.timbre :as log]))

(defn schema-validator
  "Returns a function that will validate a value against the given spec. If
   the schema check fails, then an error will be logged to the console with the
   schema failure explanation."
  [db-spec]
  (fn validate-schema [db]
    (let [valid? (s/valid? db-spec db)]
      (when-not valid?
        (log/error "failed db schema check: " (expound/expound-str db-spec db)))
      valid?)))

(def validate-schema-interceptor (after (schema-validator ::db)))

(def debug-interceptors [(when ^boolean goog.DEBUG re-frame.core/debug)
                         (when ^boolean goog.DEBUG validate-schema-interceptor)])

;;
;; full schema definition of the application
;;

(s/def ::cimi any?)

(s/def ::clients (s/nilable (only-keys :req-un [::cimi])))

(s/def ::message (s/nilable string?))

(s/def :webui.main/alert (s/nilable map?))

(s/def ::runs-data (s/nilable any?))

(s/def ::offset string?)
(s/def ::limit string?)
(s/def ::cloud string?)
(s/def ::activeOnly int?)
(s/def ::runs-params (only-keys :req-un [::offset ::limit ::cloud ::activeOnly]))

(s/def ::modules-data (s/nilable any?))

;;
;; authentication state
;;

(s/def :webui.authn/use-modal? boolean?)
(s/def :webui.authn/show-modal? boolean?)
(s/def :webui.authn/chooser-view? boolean?)

(s/def :webui.authn/id string?)
(s/def :webui.authn/label string?)
(s/def :webui.authn/group (s/nilable string?))
(s/def :webui.authn/authn-method string?)
(s/def :webui.authn/description string?)
(s/def :webui.authn/params-desc (s/map-of keyword? map?))
(s/def :webui.authn/method-defn (only-keys :req-un [:webui.authn/id
                                                    :webui.authn/label
                                                    :webui.authn/group
                                                    :webui.authn/authn-method
                                                    :webui.authn/description
                                                    :webui.authn/params-desc]))


(s/def :webui.authn/use-modal? boolean?)
(s/def :webui.authn/show-modal? boolean?)
(s/def :webui.authn/total nat-int?)
(s/def :webui.authn/count nat-int?)
(s/def :webui.authn/redirect-uri string?)
(s/def :webui.authn/error-message (s/nilable string?))
(s/def :webui.authn/session (s/nilable (s/map-of keyword? any?)))
(s/def :webui.authn/methods (s/coll-of :webui.authn/method-defn :kind vector?))
(s/def :webui.authn/forms (s/map-of string? map?))

(s/def :webui.authn/authn (only-keys :req-un [:webui.authn/use-modal?
                                              :webui.authn/show-modal?
                                              :webui.authn/chooser-view?
                                              :webui.authn/total
                                              :webui.authn/count
                                              :webui.authn/redirect-uri
                                              :webui.authn/error-message
                                              :webui.authn/session
                                              :webui.authn/methods
                                              :webui.authn/forms]))

(s/def ::id string?)
(s/def ::resourceURI string?)
(s/def ::created string?)
(s/def ::updated string?)


(s/def ::type #{"ROLE" "USER"})
(s/def ::principal string?)
(s/def ::right #{"ALL" "VIEW" "RUN" "CREATE"})
(s/def ::rule (only-keys :req-un [::principal ::type ::right]))
(s/def ::rules (s/coll-of ::rule))
(s/def ::owner (only-keys :req-un [::principal ::type]))
(s/def ::acl (only-keys :req-un [::owner ::rules]))

(s/def :cimi.cep/baseURI string?)
(s/def :cimi.cep/collection-key (s/map-of string? keyword?))
(s/def :cimi.cep/collection-href (s/map-of keyword? string?))
(s/def :cimi/cloud-entry-point (s/nilable (only-keys :req-un [:cimi.cep/baseURI
                                                              :cimi.cep/collection-key
                                                              :cimi.cep/collection-href])))

;;
;; query parameters for CIMI searches
;;
(s/def :cimi.search.query-params/$first nat-int?)
(s/def :cimi.search.query-params/$last nat-int?)
(s/def :cimi.search.query-params/$filter (s/nilable string?))
(s/def :cimi.search.query-params/$orderby (s/nilable string?))
(s/def :cimi.search.query-params/$aggregation (s/nilable string?))
(s/def :cimi.search.query-params/$select (s/nilable string?))

(s/def :cimi.search/query-params (only-keys :req-un [:cimi.search.query-params/$first
                                                     :cimi.search.query-params/$last
                                                     :cimi.search.query-params/$filter
                                                     :cimi.search.query-params/$orderby
                                                     :cimi.search.query-params/$aggregation
                                                     :cimi.search.query-params/$select]))

(s/def :cimi.search.cache/aggregations any?)
(s/def :cimi.search.cache/descriptions (s/map-of string? (s/map-of keyword? any?)))
(s/def :cimi.search.cache/resource any?)
(s/def :cimi.search.cache/resources any?)
(s/def :cimi.search/cache (s/nilable (only-keys :opt-un [:cimi.search.cache/aggregations
                                                         :cimi.search.cache/descriptions
                                                         :cimi.search.cache/resource
                                                         :cimi.search.cache/resources])))

(s/def :cimi.search.fields/available (s/coll-of string? :kind vector?))
(s/def :cimi.search.fields/selected (s/coll-of string? :kind vector?))
(s/def :cimi.search/fields (only-keys :req-un [:cimi.search.fields/available
                                               :cimi.search.fields/selected]))

(s/def :cimi.search/collection-name (s/nilable string?))
(s/def :cimi.search/completed? boolean?)

(s/def ::label string?)
(s/def ::choice (only-keys :req-un [::id ::label]))

(s/def :cimi.search.add/show-modal? boolean?)

(s/def :cimi/search (only-keys :req-un [:cimi.search/collection-name
                                        :cimi.search/query-params
                                        :cimi.search/cache
                                        :cimi.search/fields
                                        :cimi.search/completed?

                                        :cimi.search.add/show-modal?]))

;;
;; navigation information
;;
(s/def :webui.navigation/path (s/coll-of string? :kind vector?))
(s/def :webui.navigation/query-params (s/nilable map?))
(s/def :webui/navigation (only-keys :req-un [:webui.navigation/path
                                             :webui.navigation/query-params]))


;;
;; internationalization parameters
;;

(s/def :webui.i18n/locale string?)
(s/def :webui.i18n/tr fn?)
(s/def :webui.i18n/i18n (only-keys :req-un [:webui.i18n/locale :webui.i18n/tr]))

(s/def ::db (only-keys :req-un [:webui.i18n/i18n
                                :cimi/cloud-entry-point
                                ::clients ::message
                                :webui.main/alert
                                :webui/navigation
                                ::runs-data ::runs-params
                                ::modules-data
                                :webui.authn/authn
                                :cimi/search]))

;;
;; initial database value
;;

(def default-value
  {:i18n              {:locale "en"
                       :tr     (dictionary/create-tr-fn "en")}

   :clients           nil
   :message           nil
   :alert             nil

   :navigation        {:path         []
                       :query-params nil}

   :runs-data         nil
   :runs-params       {:offset     "0"
                       :limit      "10"
                       :cloud      ""
                       :activeOnly 1}

   :modules-data      nil

   :authn             {:use-modal?    true
                       :show-modal?   false
                       :chooser-view? true
                       :total         0
                       :count         0
                       :redirect-uri  "/webui/login"
                       :error-message nil
                       :session       nil
                       :methods       []
                       :forms         {}}

   :cloud-entry-point nil

   :search            {:collection-name "session"
                       :query-params    {:$first       1
                                         :$last        20
                                         :$filter      nil
                                         :$orderby     nil
                                         :$aggregation nil
                                         :$select      nil}
                       :cache           nil
                       :fields          {:available ["id"]
                                         :selected  ["id"]}

                       :completed?      true

                       :show-modal?     false}})
