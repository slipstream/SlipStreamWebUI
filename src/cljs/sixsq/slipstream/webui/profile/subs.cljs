(ns sixsq.slipstream.webui.profile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :user-id
         (fn [db _] (-> db :authn :user-id)))

