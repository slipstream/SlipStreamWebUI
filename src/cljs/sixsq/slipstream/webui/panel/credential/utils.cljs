(ns sixsq.slipstream.webui.panel.credential.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<! >! chan]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.impl.utils.http-async :as http]
    [sixsq.slipstream.client.impl.utils.json :as json]
    [taoensso.timbre :as log]))

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
  [{:keys [describe-url] :as tpl}]
  (go
    (if describe-url
      (let [params-desc (<! (http/get describe-url {:chan (chan 1 (json/body-as-json) identity)}))]
        (when-not (instance? js/Error params-desc)
          (-> tpl
              (assoc :params-desc (filter-params-desc (dissoc params-desc :acl)))
              (dissoc :describe-url)))))))

(defn prepare-session-template
  [baseURI {:keys [id name description operations] :as tpl}]
  (when-let [describe-url (->> operations
                               extract-describe-url
                               (absolute-url baseURI))]
    {:id           id
     :label        name
     :description  description
     :describe-url describe-url}))

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

(defn collection-key->template-key
  [collection-keyword]
  (keyword (str (->> collection-keyword
                     name
                     (re-matches #"^(.*)s$")
                     second)
                "Template")))

(defn create-template
  [resource-key form-data]
  (let [[common-map template-map] (split-form-data form-data)
        template-keyword (collection-key->template-key resource-key)]
    (assoc common-map template-keyword template-map)))
