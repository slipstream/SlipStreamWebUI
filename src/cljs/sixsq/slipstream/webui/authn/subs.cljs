(ns sixsq.slipstream.webui.authn.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :logged-in?
         (fn [db _] (-> db :authn :logged-in?)))

(reg-sub :authn
         (fn [db _] (-> db :authn)))

