(ns sixsq.slipstream.webui.docs.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.docs.spec :as spec]
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
    (log/error resourceMetadata)
    (log/error documents)
    (if resourceMetadata
      (some-> documents seq (get resourceMetadata))
      (some->> documents
               vals
               seq
               (filter #(= (:name %) (some-> resourceURI (str/split #"/") last)))
               first))))
