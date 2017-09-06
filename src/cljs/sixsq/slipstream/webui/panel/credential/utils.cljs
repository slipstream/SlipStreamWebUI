(ns sixsq.slipstream.webui.panel.credential.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<! >! chan]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.api.cimi.utils :as cu]
    [sixsq.slipstream.client.api.utils.http-async :as http]
    [sixsq.slipstream.client.api.utils.json :as json]
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
  [baseURI {:keys [id name description operations] :as tpl}]
  (when-let [describe-url (->> operations
                               extract-describe-url
                               (absolute-url baseURI))]
    {:id           id
     :label        name
     :description  description
     :describe-url describe-url}))

(defn extract-template-info
  [client]
  (go
    (let [baseURI (:baseURI (<! (cimi/cloud-entry-point client)))
          credential-templates (:credentialTemplates (<! (cimi/search client :credentialTemplates)))]
      (when-not (instance? js/Error credential-templates)
        (map (partial prepare-session-template baseURI) credential-templates)))))

(defn is-common-attribute?
  [attr]
  (#{:name :description :properties} attr))

(defn split-form-data
  [form-data]
  (let [common-attrs #{:name :description :properties}
        common-map (select-keys form-data common-attrs)
        template-map (into {} (remove #(common-attrs (first %)) form-data))]
    [common-map template-map]))

(defn create-template
  [form-data]
  (let [[common-map template-map] (split-form-data form-data)]
    (assoc common-map :credentialTemplate template-map)))
