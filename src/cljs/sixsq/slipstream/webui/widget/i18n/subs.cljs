(ns sixsq.slipstream.webui.widget.i18n.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :webui.i18n/locale
         (fn [db _] (-> db :i18n :locale)))

(reg-sub :webui.i18n/tr
         (fn [db _] (-> db :i18n :tr)))

