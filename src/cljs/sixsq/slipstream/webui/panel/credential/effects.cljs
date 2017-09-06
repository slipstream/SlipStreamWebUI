(ns sixsq.slipstream.webui.panel.credential.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.panel.credential.utils :as utils]
    [taoensso.timbre :as log]))

(reg-fx
  :fx.webui.credential/get-description
  (fn [[template]]
    (go
      (when-let [description (<! (utils/complete-parameter-description template))]
        (dispatch [:evt.webui.credential/set-description description])))))

(reg-fx
  :fx.webui.credential/get-templates
  (fn [[client]]
    (go
      (when-let [results (<! (utils/extract-template-info client))]
        (doseq [result results]
          (dispatch [:evt.webui.credential/get-description result]))))))

(reg-fx
  :fx.webui.credential/create-credential
  (fn [[client request-body]]
    (go
      (let [response (<! (cimi/add client :credentials request-body))]
        (log/error "Create credential response:\n" (with-out-str (cljs.pprint/pprint response)))
        (let [alert-type :danger
              heading "Create Credential Result"
              body (with-out-str (cljs.pprint/pprint response))]
          (dispatch [:evt.webui.main/raise-alert {:alert-type alert-type
                                                  :heading heading
                                                  :body body}]))))))
