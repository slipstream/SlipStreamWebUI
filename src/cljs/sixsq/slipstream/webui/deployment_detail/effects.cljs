(ns sixsq.slipstream.webui.deployment-detail.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx]]
    [sixsq.slipstream.client.api.runs :as runs]))


(reg-fx
  ::get-deployment
  (fn [[client resource-id callback]]
    (go
      (let [deployment (<! (runs/get-run client resource-id))]
        (callback deployment)))))
