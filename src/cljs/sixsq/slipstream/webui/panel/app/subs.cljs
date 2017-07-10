(ns sixsq.slipstream.webui.panel.app.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(defn modules-data [db _]
  (:modules-data db))
(reg-sub :modules-data modules-data)
