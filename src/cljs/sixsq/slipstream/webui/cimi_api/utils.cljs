(ns sixsq.slipstream.webui.cimi-api.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<! >! chan]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.impl.utils.http-async :as http]
    [sixsq.slipstream.client.impl.utils.json :as json]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]
    [clojure.walk :as walk]
    [camel-snake-kebab.core :as csk]
    [taoensso.timbre :as log]))


(def ^:const common-attrs #{:created :updated :resourceURI :properties :acl :operations})


(defn remove-common-attrs
  "Remove the CIMI common attributes from the given map."
  [m]
  (into {} (remove #(common-attrs (first %)) m)))


(defn sanitize-params [params]
  (into {} (remove (comp nil? second) params)))


(defn get-current-session
  [client]
  (go
    (let [session-collection (<! (cimi/search client :sessions))]
      (when-not (instance? js/Error session-collection)
        (-> session-collection :sessions first)))))


(defn extract-describe-action
  [{:keys [rel href] :as op}]
  (when (= rel "http://sixsq.com/slipstream/1/action/describe")
    href))


(defn extract-describe-url
  [ops]
  (->> ops
       (map extract-describe-action)
       (remove nil?)
       first))


(defn absolute-url [baseURI relative-url]
  (str baseURI relative-url))


(defn keep-param-desc? [[k {:keys [type readOnly]}]]
  (and (not readOnly) (not= "map" type) (not= "list" type)))


(defn filter-params-desc [desc]
  (into {} (filter keep-param-desc? desc)))

(defn complete-parameter-description
  [{:keys [describe-url default-values] :as tpl}]
  (go
    (if describe-url
      (let [params-desc (<! (http/get describe-url {:chan (chan 1 (json/body-as-json) identity)}))]
        (when-not (instance? js/Error params-desc)
          (let [default-values-map (->> default-values (map (fn [[k v]] [k {:data v}])) (into {}))
                description-with-defaults (merge-with merge params-desc default-values-map)
                description-filtered (filter-params-desc (dissoc description-with-defaults :acl))]
            (-> tpl
                (assoc :params-desc description-filtered)
                (dissoc :default-values)
                (dissoc :describe-url))))))))

(defn prepare-session-template ; FIXME Is used also for other things than session
  [baseURI {:keys [id name group method description operations] :as tpl}]
  (when-let [describe-url (->> operations
                               extract-describe-url
                               (absolute-url baseURI))]
    {:id           id
     :label        name
     :group        group
     :authn-method method
     :default-values (-> tpl
                         (cimi-utils/strip-common-attrs)
                         (cimi-utils/strip-service-attrs)
                         (dissoc :acl))
     :description  description
     :describe-url describe-url}))


(defn login-form-fields [{:keys [params-desc] :as tpl}]
  (->> params-desc
       keys
       (map (fn [k] [k ""]))
       (into {})))


(defn extract-template-info
  [client]
  (go
    (let [baseURI (:baseURI (<! (cimi/cloud-entry-point client)))
          session-templates (:sessionTemplates (<! (cimi/search client :sessionTemplates)))]
      (if-not (instance? js/Error session-templates)
        (doall (map (partial prepare-session-template baseURI) session-templates))))))


(defn clear-form-data [m]
  (let [f (fn [[k v]] (if (string? v) [k ""] [k v]))]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))


(defn get-templates
  [client collection-keyword]
  (go
    (let [baseURI (:baseURI (<! (cimi/cloud-entry-point client)))
          collection-response (<! (cimi/search client collection-keyword))]
      (when-not (instance? js/Error collection-response)
        (->> (get collection-response collection-keyword)
             (map (partial prepare-session-template baseURI)))))))

(defn split-form-data
  [form-data]
  (let [common-attrs #{:name :description :properties}
        common-map (select-keys form-data common-attrs)
        template-map (into {} (remove #(common-attrs (first %)) form-data))]
    [common-map template-map]))

(defn create-template
  [resource-type form-data]
  (log/info "resource-type: " resource-type)
  (let [[common-map template-map] (split-form-data form-data)
        template-keyword (-> resource-type (str "Template") csk/->camelCase keyword)]
    (assoc common-map template-keyword template-map)))
