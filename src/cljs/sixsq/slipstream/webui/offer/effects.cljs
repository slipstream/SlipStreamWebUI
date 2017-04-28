(ns sixsq.slipstream.webui.offer.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]))

;; usage: (dispatch [:search client resource-type])
;; queries the given resource
(reg-fx
  :cimi/offer
  (fn [[client resource-type params]]
    (go
      (let [results (<! (cimi/search client resource-type params))]
        (dispatch [:show-offer-results resource-type results])))))
