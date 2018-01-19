(ns sixsq.slipstream.webui.application.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.application.spec :as application-spec]))


(reg-sub
  ::completed?
  (fn [db]
    (::application-spec/completed? db)))


(reg-sub
  ::module-id
  (fn [db _]
    (::application-spec/module-id db)))


(reg-sub
  ::module
  (fn [db _]
    (::application-spec/module db)))
