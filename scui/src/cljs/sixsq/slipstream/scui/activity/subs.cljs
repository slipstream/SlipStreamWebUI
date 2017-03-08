(ns sixsq.slipstream.scui.activity.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :runs-data
         (fn [db _] (-> db :runs-data)))

