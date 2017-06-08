(ns sixsq.slipstream.webui.panel.authn.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.panel.authn.utils :as au]
    [taoensso.timbre :as log]))

(reg-fx
  :fx.webui.authn/logout
  (fn [[client]]
    (go
      (let [resp (<! (cimi/logout client))]
        (if (= 200 (:status resp))
          (dispatch [:evt.webui.authn/logged-out])
          (dispatch [:message "logout failed"]))))))

(reg-fx
  :fx.webui.authn/login
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
  :fx.webui.authn/check-session
  (fn [[client]]
    (go
      (let [session (<! (au/get-current-session client))]
        (if session
          (dispatch [:evt.webui.authn/logged-in session])
          (dispatch [:evt.webui.authn/logged-out]))))))

;;
;; Downloads the session templates from the server.  Strips unnecessary
;; information and provides absolute URL for the parameter description.
;; Triggers event (and then effect) to download the parameter description.
;;
(reg-fx
  :fx.webui.authn/initialize
  (fn [[client]]
    (go
      (let [tpls (<! (au/extract-template-info client))]
        (doseq [tpl tpls]
          (dispatch [:evt.webui.authn/process-template tpl]))))))

;;
;; Downloads the parameter description from the server and adds this to
;; the template.  This is the minimized template with only the URL for
;; the parameter description.
;;
;; If the description cannot be found, then the login method will be
;; ignored.
;;
(reg-fx
  :fx.webui.authn/process-template
  (fn [[tpl]]
    (go
      (if-let [prepared-template (<! (au/complete-parameter-description tpl))]
        (dispatch [:evt.webui.authn/add-template prepared-template])))))
