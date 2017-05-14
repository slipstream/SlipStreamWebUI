(ns sixsq.slipstream.webui
  (:require
    [clojure.string :as str]
    [devtools.core :as devtools]
    [reagent.core :as reagent]
    [re-frame.core :refer [dispatch dispatch-sync]]
    [taoensso.timbre :as timbre]

    [sixsq.slipstream.webui.routes]
    [sixsq.slipstream.webui.utils :as utils]

    ;; must include these to ensure that they are not elided
    [sixsq.slipstream.webui.main.events]
    [sixsq.slipstream.webui.main.subs]
    [sixsq.slipstream.webui.main.views]))

;;
;; debugging tools
;;
;; Turn this on or off with:
;;
;; {:compiler-options {:closure-defines {'sixsq.slipstream.webui/DEV false}}
;;
(goog-define DEV false)
(if (identical? DEV true)
  (do
    (devtools/install!)
    (enable-console-print!)
    (timbre/set-level! :info))
  (timbre/set-level! :warn))


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
  (.log js/console "using slipstream server:" @SLIPSTREAM_URL)
  (.log js/console "using path prefix:" @PATH_PREFIX)
  (dispatch-sync [:initialize-db])
  (dispatch-sync [:initialize-client @SLIPSTREAM_URL])
  (dispatch-sync [:fetch-cloud-entry-point])
  (dispatch-sync [:initialize-history @PATH_PREFIX])
  (dispatch-sync [:evt.webui.authn/initialize])
  (dispatch [:evt.webui.authn/check-session])
  (reagent/render [sixsq.slipstream.webui.main.views/app]
                  (.getElementById js/document "container")))
