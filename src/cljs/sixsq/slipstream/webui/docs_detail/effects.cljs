(ns sixsq.slipstream.webui.docs-detail.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.runs :as runs]))


(reg-fx
  ::get-deployment
  (fn [[client resource-id callback]]
    (go
      (let [deployment (<! (runs/get-run client resource-id))]
        (callback deployment)))))


(reg-fx
  ::terminate-deployment
  (fn [[client uuid]]
    (go
      (let [result (<! (runs/terminate-run client uuid))
            error (when (instance? js/Error result)
                    (:error (js->clj
                              (->> result ex-data :body (.parse js/JSON))
                              :keywordize-keys true)))]
        (if error
          (js/alert (str "Terminate of " uuid " failed: " error))
          (js/alert (str "Terminate of " uuid " succeeded.")))))))
