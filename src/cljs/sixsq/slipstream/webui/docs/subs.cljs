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
  (fn [documents [_ {:keys [resourceMetadata resourceURI] :as resource}]]
    (if-let [resourceMetadatas (-> documents vals seq)]
      (do (log/error "NO DISPATCH!!!!" resourceMetadatas)
          (if resourceMetadata
            (get documents resourceMetadata)
            (some->> resourceMetadatas
                     (filter #(= (:name %) (some-> resourceURI (str/split #"/") last)))
                     first)))
      (do
        (log/error "DISPATCH!!!!")
        (dispatch [::events/get-documents])))))

