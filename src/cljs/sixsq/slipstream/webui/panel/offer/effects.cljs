(ns sixsq.slipstream.webui.panel.offer.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]))

;; usage: (dispatch [:cimi/offer client resource-type])
;; queries the given resource
(reg-fx
  :fx.webui.offer/list
  (fn [[client resource-type params]]
    (go
      (let [results (<! (cimi/search client resource-type params))]
        (dispatch [:evt.webui.offer/show-results resource-type results])))))

;; usage: (dispatch [:cimi/offer-detail client resource-type])
;; queries the given resource
(reg-fx
  :fx.webui.offer/detail
  (fn [[client resource-type uuid]]
    (go
      (let [result (<! (cimi/get client (str "service-offer/" uuid)))] ;; FIXME: Remove hardcoded value.
        (dispatch [:evt.webui.offer/set-data result])))))
