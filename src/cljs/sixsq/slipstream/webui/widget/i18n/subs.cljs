(ns sixsq.slipstream.webui.widget.i18n.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :i18n-locale
         (fn [db _] (-> db :i18n :locale)))

(reg-sub :i18n-tr
         (fn [db _] (-> db :i18n :tr)))

