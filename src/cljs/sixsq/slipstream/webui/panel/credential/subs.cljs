(ns sixsq.slipstream.webui.panel.credential.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :webui.credential/descriptions-vector
         (fn [db _] (->> db :credential :descriptions vals (sort :id))))

(reg-sub :webui.credential/show-modal?
         (fn [db _] (-> db :credential :show-modal?)))

(reg-sub :webui.credential/available-fields
         (fn [db _] (->> db :credential :fields :available (map (fn [v] {:id v :label v})))))

(reg-sub :webui.credential/selected-fields
         (fn [db _] (-> db :credential :fields :selected)))

(reg-sub :webui.credential/collection-name
         (fn [db _] (-> db :credential :collection-name)))

(reg-sub :webui.credential/resources
         (fn [db _] (-> db :credential :cache :resources)))
