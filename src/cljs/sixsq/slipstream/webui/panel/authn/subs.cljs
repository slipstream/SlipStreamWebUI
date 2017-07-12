(ns sixsq.slipstream.webui.panel.authn.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :webui.authn/error-message
         (fn [db _] (-> db :authn :error-message)))

(reg-sub :webui.authn/session
         (fn [db _] (-> db :authn :session)))

(reg-sub :webui.authn/total
         (fn [db _] (-> db :authn :total)))

(reg-sub :webui.authn/count
         (fn [db _] (-> db :authn :count)))

(reg-sub :webui.authn/methods
         (fn [db _] (-> db :authn :methods)))

(reg-sub :webui.authn/forms
         (fn [db _] (-> db :authn :forms)))

(reg-sub :webui.authn/redirect-uri
         (fn [db _] (-> db :authn :redirect-uri)))

