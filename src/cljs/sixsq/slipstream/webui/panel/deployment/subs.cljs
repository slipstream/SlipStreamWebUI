(ns sixsq.slipstream.webui.panel.deployment.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :runs-data
         (fn [db _] (-> db :runs-data)))

