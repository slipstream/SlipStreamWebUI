(ns sixsq.slipstream.authn.main.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub :resource-query-params
         (fn [db _] (-> db :resource-query-params)))
