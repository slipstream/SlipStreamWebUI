(ns sixsq.slipstream.webui.i18n.subs
  (:require
    [re-frame.core :refer [reg-sub]]))


(reg-sub
  ::locale
  (fn [db]
    (:sixsq.slipstream.webui.i18n.spec/locale db)))


(reg-sub
  ::tr
  (fn [db]
    (:sixsq.slipstream.webui.i18n.spec/tr db)))
