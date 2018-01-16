(ns
  ^{:copyright "Copyright 2017, SixSq Sàrl"
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.deployment.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.runs :as runs]))


(reg-fx
  ::get-deployments
  (fn [[client params]]
    (go
      (let [deployments (<! (runs/search-runs client params))]
        (dispatch [:cubic.deployment.events/set-deployments deployments])))))
