(ns sixsq.slipstream.webui.panel.credential.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :webui.credential/descriptions-vector
         (fn [db _] (->> db :credentials :descriptions vals (sort :id))))

(reg-sub :webui.credential/show-modal?
         (fn [db _] (-> db :credentials :show-modal?)))
