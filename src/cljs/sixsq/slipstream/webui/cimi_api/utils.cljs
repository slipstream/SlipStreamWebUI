(ns sixsq.slipstream.webui.cimi-api.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [camel-snake-kebab.core :as csk]
    [cljs.core.async :refer [<! >! chan]]
    [clojure.walk :as walk]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.impl.utils.http-async :as http]
    [sixsq.slipstream.client.impl.utils.json :as json]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]
    [taoensso.timbre :as log]))


(def ^:const common-attrs #{:id :created :updated :resourceURI :properties :acl :operations})


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


(defn absolute-url [baseURI relative-url]
  (str baseURI relative-url))


(defn keep-param-desc? [[k {:keys [type readOnly]}]]
  (and (not readOnly) (not= "map" type) (not= "list" type)))


(defn filter-params-desc [desc]
  (into {} (filter keep-param-desc? desc)))


(defn prepare-template
  [{:keys [id name method description group hidden icon order] :as tpl}]
  [(keyword id) {:id             id
                 :label          name
                 :method         method
                 :group          group
                 :hidden         hidden
                 :icon           icon
                 :order          order
                 :default-values (remove-common-attrs tpl)
                 :description    description}])


(defn login-form-fields [{:keys [params-desc] :as tpl}]
  (->> params-desc
       keys
       (map (fn [k] [k ""]))
       (into {})))


(defn clear-form-data [m]
  (let [f (fn [[k v]] (if (string? v) [k ""] [k v]))]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))


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
        template-keyword (-> resource-type (str "Template") csk/->camelCase keyword)] ; FIXME
    (assoc common-map template-keyword template-map)))
