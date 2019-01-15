(ns sixsq.slipstream.webui.docs.subs
  (:require
    [re-frame.core :refer [reg-sub dispatch]]
    [sixsq.slipstream.webui.docs.spec :as spec]
    [sixsq.slipstream.webui.docs.events :as events]
    [clojure.string :as str]
    [taoensso.timbre :as log]))


(reg-sub
  ::loading?
  (fn [db]
    (::spec/loading? db)))


(reg-sub
  ::documents
  (fn [db]
    (::spec/documents db)))


(reg-sub
  ::document
  :<- [::documents]
  (fn [documents [_ {:keys [id resourceMetadata resourceURI] :as resource}]]
    (log/error id resourceMetadata resourceURI)
    (if (seq documents)
      (let [resource-metadata-id (cond
                                   resourceMetadata resourceMetadata
                                   (re-find #"-template/" (str id)) (->> (str/replace id #"/" "-")
                                                                         (str "resource-metadata/"))
                                   :else (let [collection-name (-> resourceURI str (str/split #"/") last)]
                                           (str "resource-metadata/" collection-name)))]
        (log/error "search for metadata id:" resource-metadata-id (sort (keys documents)))
        (get documents "resource-metadata/credential-template-api-key" #_resource-metadata-id))
      (dispatch [::events/get-documents]))))

