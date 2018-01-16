(ns
  ^{:copyright "Copyright 2017, SixSq Sàrl"
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.cimi-api.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<! >! chan]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.impl.utils.http-async :as http]
    [sixsq.slipstream.client.impl.utils.json :as json]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [clojure.walk :as walk]))


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


(defn keep-param-desc? [[k {:keys [mandatory readOnly]}]]
  (and mandatory (not readOnly)))


(defn filter-params-desc [desc]
  (into {} (filter keep-param-desc? desc)))


(defn complete-parameter-description
  [{:keys [describe-url] :as tpl}]
  (go
    (if describe-url
      (let [params-desc (<! (http/get describe-url {:chan (chan 1 (json/body-as-json) identity)}))]
        (when-not (instance? js/Error params-desc)
          (-> tpl
              (assoc :params-desc (filter-params-desc params-desc))
              (dissoc :describe-url)))))))


(defn prepare-session-template
  [baseURI {:keys [id name group method description operations] :as tpl}]
  (when-let [describe-url (->> operations
                               extract-describe-url
                               (absolute-url baseURI))]
    {:id           id
     :label        name
     :group        group
     :authn-method method
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
