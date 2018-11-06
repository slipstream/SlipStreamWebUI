(ns sixsq.slipstream.webui.docs.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.docs.spec :as spec]))


(reg-sub
  ::loading?
  (fn [db]
    (::spec/loading? db)))


(reg-sub
  ::documents
  (fn [db]
    (::spec/documents db)))

