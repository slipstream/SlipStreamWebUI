(ns sixsq.slipstream.webui.panel.authn.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :webui.authn/use-modal?
         (fn [db _] (-> db :authn :use-modal?)))

(reg-sub :webui.authn/show-modal?
         (fn [db _] (-> db :authn :show-modal?)))

(reg-sub :webui.authn/chooser-view?
         (fn [db _] (-> db :authn :chooser-view?)))

(reg-sub :webui.authn/error-message
         (fn [db _] (-> db :authn :error-message)))

(reg-sub :webui.authn/session
         (fn [db _] (-> db :authn :session)))

(reg-sub :webui.authn/use-modal?
         (fn [db _] (-> db :authn :use-modal?)))

(reg-sub :webui.authn/show-modal?
         (fn [db _] (-> db :authn :show-modal?)))

(reg-sub :webui.authn/total
         (fn [db _] (-> db :authn :total)))

(reg-sub :webui.authn/count
         (fn [db _] (-> db :authn :count)))

(reg-sub :webui.authn/loading?
         (fn [_ _]
           [(subscribe [:webui.authn/count])
            (subscribe [:webui.authn/total])])
         (fn [[count total] _] false                        ;(or (zero? total) (not= count total))
           ))

(reg-sub :webui.authn/methods
         (fn [db _] (-> db :authn :methods)))

(reg-sub :webui.authn/forms
         (fn [db _] (-> db :authn :forms)))

(reg-sub :webui.authn/redirect-uri
         (fn [db _] (-> db :authn :redirect-uri)))

