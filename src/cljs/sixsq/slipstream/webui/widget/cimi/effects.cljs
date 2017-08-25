(ns sixsq.slipstream.webui.widget.cimi.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.panel.authn.utils :as au]
    [taoensso.timbre :as log]))

(reg-fx
  :fx.webui.cimi/logout
  (fn [[client]]
    (go
      (let [resp (<! (cimi/logout client))]
        (if (= 200 (:status resp))
          (dispatch [:evt.webui.authn/logged-out])
          (dispatch [:message "logout failed"]))))))

(reg-fx
  :fx.webui.cimi/login
  (fn [[client creds]]
    (go
      (let [resp (<! (cimi/login client creds))]
        (case (:status resp)
          201 (let [session (<! (au/get-current-session client))]
                (dispatch [:evt.webui.authn/logged-in session]))
          303 (let [session (<! (au/get-current-session client))]
                (dispatch [:evt.webui.authn/logged-in session]))
          (do
            (log/error "Error login response:" (with-out-str (cljs.pprint/pprint resp)))
            (dispatch [:message "login failed"])))))))

(reg-fx
  :fx.webui.cimi/search
  (fn [[client resource-type params]]
    (go
      (let [results (<! (cimi/search client resource-type params))]
        (dispatch [:show-search-results resource-type results])))))

(reg-fx
  :fx.webui.op/add
  (fn [[client resource-type data]]
    (go
      (let [json-data (-> (.parse js/JSON (clj->js data) nil 2)
                          (js->clj :keywordize-keys true))]
        (log/error "DEBUG" "ADD" resource-type data json-data)
        (let [resp (<! (cimi/add client resource-type json-data))]
          (log/error "DEBUG" "RESPONSE" resp)
          (let [{:keys [status message] :as resp} resp
                state (if (= 201 status) "SUCCESS" "FAIL")]
            (dispatch [:message (str state ": " message "\n" resp)])))))))

(reg-fx
  :fx.webui.op/edit
  (fn [[client href data]]
    (log/error "DEBUG" "DELETE" href)
    (go
      (let [{:keys [status message] :as resp} (<! (cimi/edit client href data))
            state (if (= 200 status) "SUCCESS" "FAIL")]
        (dispatch [:message (str state ": editing " href "\n" message)])))))

(reg-fx
  :fx.webui.op/delete
  (fn [[client href]]
    (log/error "DEBUG" "DELETE" href)
    (go
      (let [{:keys [status message] :as resp} (<! (cimi/delete client href))
            state (if (= 200 status) "SUCCESS" "FAIL")]
        (dispatch [:message (str state ": deleting " href "\n" message)])))))
