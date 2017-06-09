(ns sixsq.slipstream.webui.panel.authn.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :webui.authn/error-message
         (fn [db _] (-> db :authn :error-message)))

(reg-sub :webui.authn/session
         (fn [db _] (-> db :authn :session)))

(reg-sub :webui.authn/method
         (fn [db _] (-> db :authn :method)))

(reg-sub :webui.authn/methods
         (fn [db _] (-> db :authn :methods)))

(reg-sub :webui.authn/forms
         (fn [db _] (-> db :authn :forms)))

