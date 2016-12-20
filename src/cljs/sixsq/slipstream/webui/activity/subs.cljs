(ns sixsq.slipstream.webui.activity.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(defn runs-data
  [db _]
  (:runs-data db))
(reg-sub :runs-data runs-data)

