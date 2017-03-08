(ns sixsq.slipstream.scui.offers.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]))

;; usage: (dispatch [:cloud-entry-point client])
;; fetches the cloud entry point
(reg-fx
  :cimi/cloud-entry-point
  (fn [[client]]
    (go
      (if-let [cep (<! (cimi/cloud-entry-point client))]
        (dispatch [:insert-cloud-entry-point cep])
        (dispatch [:message "loading cloud-entry-point failed"])))))

;; usage: (dispatch [:search client resource-type])
;; queries the given resource
(reg-fx
  :cimi/search
  (fn [[client resource-type params]]
    (go
      (let [results (<! (cimi/search client resource-type params))]
        (dispatch [:show-search-results resource-type results])))))
