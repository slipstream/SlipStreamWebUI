(ns sixsq.slipstream.webui.widget.authn.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :logged-in?
         (fn [db _] (-> db :authn :logged-in?)))

(reg-sub :show-login-dialog?
         (fn [db _] (-> db :authn :show-login-dialog?)))

(reg-sub :authn
         (fn [db _] (-> db :authn)))

