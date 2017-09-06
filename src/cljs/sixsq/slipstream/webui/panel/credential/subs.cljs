(ns sixsq.slipstream.webui.panel.credential.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :webui.credential/descriptions
         (fn [db _] (-> db :credentials :descriptions)))

(reg-sub :webui.credential/form-data
         (fn [db _] (-> db :credentials :form-data)))

(reg-sub :webui.credential/show-modal?
         (fn [db _] (-> db :credentials :show-modal?)))
