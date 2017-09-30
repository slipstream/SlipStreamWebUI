(ns sixsq.slipstream.authn.main.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub :webui.main/nav-query-params
         (fn [db _] (-> db :navigation :query-params)))
