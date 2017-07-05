(ns sixsq.slipstream.webui.db
  (:require-macros [sixsq.slipstream.webui.db :refer [only-keys]])
  (:require
    [clojure.spec.alpha :as s]
    [re-frame.core :refer [after]]
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
        (log/error "failed db schema check: " (s/explain-str db-spec db)))
      valid?)))

(def validate-schema-interceptor (after (schema-validator ::db)))

(def debug-interceptors [(when ^boolean goog.DEBUG re-frame.core/debug)
                         (when ^boolean goog.DEBUG validate-schema-interceptor)])

;;
;; full schema definition of the application
;;

(s/def ::cimi any?)
(s/def ::runs any?)
(s/def ::modules any?)

(s/def ::clients (s/nilable (only-keys :req-un [::cimi ::runs ::modules])))

(s/def ::message (s/nilable string?))

(s/def ::resource-data (s/nilable any?))

(s/def ::runs-data (s/nilable any?))

(s/def ::offset string?)
(s/def ::limit string?)
(s/def ::cloud string?)
(s/def ::activeOnly int?)
(s/def ::runs-params (only-keys :req-un [::offset ::limit ::cloud ::activeOnly]))

(s/def ::modules-data (s/nilable any?))
(s/def ::modules-path (s/nilable string?))
(s/def ::modules-breadcrumbs (s/nilable (s/coll-of string?)))

;;
;; authentication state
;;

(s/def :webui.authn/session (s/nilable (s/map-of keyword? any?)))

(s/def :webui.authn/id string?)
(s/def :webui.authn/label string?)
(s/def :webui.authn/authn-method string?)
(s/def :webui.authn/description string?)
(s/def :webui.authn/error-message (s/nilable string?))
(s/def :webui.authn/redirect-uri string?)
(s/def :webui.authn/params-desc (s/map-of keyword? map?))
(s/def :webui.authn/method-defn (only-keys :req-un [:webui.authn/id
                                                    :webui.authn/label
                                                    :webui.authn/authn-method
                                                    :webui.authn/description
                                                    :webui.authn/params-desc]))
(s/def :webui.authn/methods (s/coll-of :webui.authn/method-defn))
(s/def :webui.authn/forms (s/map-of string? map?))

(s/def :webui.authn/authn (only-keys :req-un [:webui.authn/redirect-uri
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

(s/def ::baseURI string?)

(s/def ::cloud-entry-point (s/nilable (s/keys :req-un [::id ::created ::updated ::acl ::baseURI])))

(s/def ::cimi-filter (s/nilable string?))
(s/def ::page nat-int?)
(s/def ::page-size (s/int-in 1 50))

(s/def ::webui (only-keys :req-un [::cimi-filter ::page ::page-size]))

(s/def ::$first nat-int?)
(s/def ::$last nat-int?)
(s/def ::$filter (s/nilable string?))

(s/def ::params (only-keys :req-un [::$first ::$last ::$filter]))

(s/def ::collection-name (s/nilable string?))
(s/def ::listing (s/nilable any?))
(s/def ::completed? boolean?)

(s/def ::label string?)
(s/def ::choice (only-keys :req-un [::id ::label]))
(s/def ::available-fields (s/coll-of ::choice))
(s/def ::selected-fields (s/coll-of ::id))

(s/def ::search (only-keys :req-un [::collection-name ::params ::listing ::completed?
                                    ::available-fields ::selected-fields]))

(s/def ::offer-data (s/nilable any?))
(s/def ::offer ::search)

(s/def ::resource-path (s/coll-of string?))
(s/def ::resource-query-params (s/nilable map?))

;;
;; internationalization parameters
;;

(s/def :webui.i18n/locale string?)
(s/def :webui.i18n/tr fn?)
(s/def :webui.i18n/i18n (only-keys :req-un [:webui.i18n/locale :webui.i18n/tr]))

(s/def ::db (only-keys :req-un [:webui.i18n/i18n
                                ::clients ::message ::resource-data ::resource-path ::resource-query-params
                                ::runs-data ::runs-params
                                ::modules-data ::modules-path ::modules-breadcrumbs
                                :webui.authn/authn ::cloud-entry-point ::search
                                ::offer-data ::offer]))

;;
;; initial database value
;;

(def default-value
  {:i18n                  {:locale "en"
                           :tr     (dictionary/create-tr-fn "en")}

   :clients               nil
   :message               nil
   :resource-data         nil
   :resource-path         []
   :resource-query-params nil

   :runs-data             nil
   :runs-params           {:offset     "0"
                           :limit      "10"
                           :cloud      ""
                           :activeOnly 1}

   :modules-data          nil
   :modules-path          nil
   :modules-breadcrumbs   nil

   :authn                 {:redirect-uri  "/webui/login"
                           :error-message nil
                           :session       nil
                           :methods       []
                           :forms         {}}

   :cloud-entry-point     nil

   :search                {:collection-name  "sessions"
                           :params           {:$first  1
                                              :$last   20
                                              :$filter nil}
                           :listing          nil
                           :completed?       true
                           :available-fields [{:id "id" :label "id"}
                                              {:id "beta" :label "beta"}]
                           :selected-fields  #{"id"}}

   :offer-data            nil
   :offer                 {:collection-name  "serviceOffers"
                           :params           {:$first  1
                                              :$last   20
                                              :$filter nil}
                           :listing          nil
                           :completed?       true
                           :available-fields [{:id "id" :label "id"}
                                              {:id "beta" :label "beta"}]
                           :selected-fields  #{"id"}}})
