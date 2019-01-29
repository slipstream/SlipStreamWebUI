(ns sixsq.slipstream.webui.application.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.application.spec :as spec]))


(reg-sub
  ::completed?
  ::spec/completed?)


(reg-sub
  ::module
  ::spec/module)


(reg-sub
  ::add-modal-visible?
  ::spec/add-modal-visible?)


(reg-sub
  ::add-data
  ::spec/add-data)


(reg-sub
  ::active-tab
  ::spec/active-tab)

(reg-sub
  ::add-modal-step
  (fn [db]
    (::spec/add-modal-step db)))


(reg-sub
  ::active-tab-application
  (fn [db]
    (::spec/active-tab-application db)))
