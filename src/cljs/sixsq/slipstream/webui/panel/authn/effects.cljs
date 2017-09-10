(ns sixsq.slipstream.webui.panel.authn.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.panel.authn.utils :as au]
    [taoensso.timbre :as log]))

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
        (dispatch [:evt.webui.authn/set-methods-total (count tpls)])
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
