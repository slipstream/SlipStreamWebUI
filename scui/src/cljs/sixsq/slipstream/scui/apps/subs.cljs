(ns sixsq.slipstream.scui.apps.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(defn modules-data
  [db _]
  (:modules-data db))
(reg-sub :modules-data modules-data)

(defn modules-breadcrumbs
  [db _]
  (:modules-breadcrumbs db))
(reg-sub :modules-breadcrumbs modules-breadcrumbs)
