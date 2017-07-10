(ns sixsq.slipstream.webui.panel.cimi.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]))

;; usage: (dispatch [:search client resource-type])
;; queries the given resource
(reg-fx
  :fx.webui.cimi/search
  (fn [[client resource-type params]]
    (go
      (let [results (<! (cimi/search client resource-type params))]
        (dispatch [:show-search-results resource-type results])))))
