(ns sixsq.slipstream.webui.widget.operations.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.panel.authn.utils :as au]
    [taoensso.timbre :as log]))

(reg-fx
  :fx.webui.op/add
  (fn [[client resource-type data]]
    (go
      (log/error "DEBUG" "ADD" resource-type data)
      (let [resource-type :credentials
            data {:credentialTemplate {:href "credential-template/generate-api-key"}}]
        (log/error "DEBUG" "ADD" resource-type data)
        (let [cep (<! (cimi/cloud-entry-point client))
              resp (<! (cimi/add client resource-type data))]
          (log/error "DEBUG" "CEP" cep)
          (log/error "DEBUG" "RESPONSE" resp)
          (if (= 201 (:status resp))
            (dispatch [:message (str "creation of " resource-type " succeeded")])
            (dispatch [:message (str "creation of " resource-type " failed")])))))))

(reg-fx
  :fx.webui.op/edit
  (fn [[client href data]]
    (go
      (let [resp (<! (cimi/edit client href data))]
        (if (= 200 (:status resp))
          (dispatch [:message (str "editing of " href " succeeded")])
          (dispatch [:message (str "editing of " href " failed")]))))))

(reg-fx
  :fx.webui.op/delete
  (fn [[client href]]
    (log/error "DEBUG" "DELETE" href)
    (go
      (let [resp (<! (cimi/delete client href))]
        (if (= 200 (:status resp))
          (dispatch [:message (str "deleting " href " succeeded")])
          (dispatch [:message (str "deleting " href " failed")]))))))


