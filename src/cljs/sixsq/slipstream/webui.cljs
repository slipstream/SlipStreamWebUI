(ns sixsq.slipstream.webui
  (:require
    [clojure.string :as str]
    [reagent.core :as reagent]
    [re-frame.core :refer [dispatch dispatch-sync]]
    [taoensso.timbre :as log]

    [sixsq.slipstream.webui.defines :as defines]
    [sixsq.slipstream.webui.routes]
    [sixsq.slipstream.webui.utils :as utils]

    ;; must include these to ensure that they are not elided
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [cljsjs.codemirror.mode.javascript]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.main.events]
    [sixsq.slipstream.webui.main.subs]
    [sixsq.slipstream.webui.main.views]

    [sixsq.slipstream.webui.widget.history.events]))

;;
;; This option is not compatible with other platforms, notably nodejs.
;; Use instead the logging calls to provide console output.
;;
(enable-console-print!)

;;
;; debugging log level
;;
(log/set-level! (keyword defines/LOGGING_LEVEL))

;;
;; determine the host url
;;
(def SLIPSTREAM_URL (delay (if-not (str/blank? defines/HOST_URL) defines/HOST_URL (utils/host-url))))

;;
;; determine the web application prefix
;;
(def PATH_PREFIX (delay (str (utils/host-url) defines/CONTEXT)))

;;
;; hook to initialize the web application
;;
(defn ^:export init
  []
  (log/info "using slipstream server:" @SLIPSTREAM_URL)
  (log/info "using path prefix:" @PATH_PREFIX)
  (dispatch-sync [:evt.webui.main/initialize-db])
  (dispatch-sync [:evt.webui.main/initialize-client @SLIPSTREAM_URL])
  (dispatch-sync [:evt.webui.main/load-cloud-entry-point])
  (dispatch-sync [:evt.webui.main/set-host-theme])
  (dispatch-sync [:evt.webui.history/initialize @PATH_PREFIX])
  (dispatch-sync [:evt.webui.authn/initialize])
  (dispatch-sync [:evt.webui.authn/set-redirect-uri "/webui/login"])
  (dispatch [:evt.webui.authn/check-session])
  ;; FIXME: TESTING
  #_(dispatch [:evt.webui.credential/get-templates])
  #_(dispatch [:evt.webui.cimi/get-templates "collection-template"])
  (when-let [header-element (.getElementById js/document "webui-header")]
    (reagent/render [sixsq.slipstream.webui.main.views/header] header-element))
  (when-let [footer-element (.getElementById js/document "webui-footer")]
    (reagent/render [sixsq.slipstream.webui.main.views/footer] footer-element))
  (when-let [container-element (.getElementById js/document "webui-container")]
    (reagent/render [sixsq.slipstream.webui.main.views/app] container-element)))
