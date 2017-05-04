(ns sixsq.slipstream.webui.panel.deployment.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.runs :as runs]))

;; usage: (dispatch [:runs-search client])
;; queries the given resource
(reg-fx
  :runs/search
  (fn [[client params]]
    (go
      (let [results (<! (runs/search client params))]
        (dispatch [:set-runs-data results])))))
