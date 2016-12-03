(ns sixsq.slipstream.webui.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]))

;;
;; effects
;;

;; usage: (dispatch [:logout])
;; logs the user out of the client
(reg-fx
  :cimi/logout
  (fn [[client]]
    (go
      (let [status (<! (cimi/logout client))]
        (if (= 200 status)
          (dispatch [:logged-out])
          (dispatch [:message "logout failed"]))))))

;; usage: (dispatch [:login creds])
;; logs the user into SlipStream
(reg-fx
  :cimi/login
  (fn [[client {:keys [username] :as creds}]]
    (go
      (let [{:keys [login-status]} (<! (cimi/login client creds))]
        (if (= 200 login-status)
          (dispatch [:logged-in username])
          (dispatch [:message "login failed"]))))))

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
