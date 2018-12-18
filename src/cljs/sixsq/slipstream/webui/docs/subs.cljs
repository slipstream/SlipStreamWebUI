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
    (if-let [resourceMetadataSeq (-> documents vals seq)]
      (cond
        resourceMetadata (get documents resourceMetadata)
        (re-find #"-template/" (str id)) (->> resourceMetadataSeq
                                              (filter #(= (:id %) (str "resource-metadata/"
                                                                       (str/replace id #"/" "-"))))
                                              first)
        :else (->> resourceMetadataSeq
                   (filter #(= (:name %) (-> resourceURI str (str/split #"/") last)))
                   first))
      (dispatch [::events/get-documents]))))

