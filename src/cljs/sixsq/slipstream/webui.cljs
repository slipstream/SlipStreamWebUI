(ns sixsq.slipstream.webui
  (:require
    [clojure.string :as str]
    [reagent.core :as reagent]
    [re-frame.core :refer [dispatch dispatch-sync]]
    [taoensso.timbre :as log]

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
;; Set the value like this:
;;
;; {:compiler-options {:closure-defines {'sixsq.slipstream.webui/LOGGING_LEVEL "info"}}
;;
(goog-define LOGGING_LEVEL "info")
(log/set-level! (keyword LOGGING_LEVEL))

;;
;; determine the host url
;;
;; Set a fixed SlipStream endpoint (useful for development) with:
;;
;; {:compiler-options {:closure-defines {'sixsq.slipstream.webui/HOST_URL "https://nuv.la"}}
;;
;; NOTE: When using an endpoint other than the one serving the javascript code
;; you MUST turn off the XSS protections of the browser.
;;
(goog-define HOST_URL "")
(def SLIPSTREAM_URL (delay (if-not (str/blank? HOST_URL) HOST_URL (utils/host-url))))

;;
;; determine the web application prefix
;;
;; The default is to concatenate '/webui' to the end of the SLIPSTREAM_URL.
;; If the application is mounted elsewhere, you can change the default with:
;;
;; {:compiler-options {:closure-defines {'sixsq.slipstream.webui/CONTEXT ""}}
;;
(goog-define CONTEXT "/webui")
(def PATH_PREFIX (delay (str (utils/host-url) CONTEXT)))

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
  (when-let [header-element (.getElementById js/document "webui-header")]
    (reagent/render [sixsq.slipstream.webui.main.views/header] header-element))
  (when-let [footer-element (.getElementById js/document "webui-footer")]
    (reagent/render [sixsq.slipstream.webui.main.views/footer] footer-element))
  (when-let [container-element (.getElementById js/document "webui-container")]
    (reagent/render [sixsq.slipstream.webui.main.views/app] container-element)))
