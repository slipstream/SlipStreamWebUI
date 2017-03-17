(ns sixsq.slipstream.webui
  (:require
    [clojure.string :as str]
    [reagent.core :as reagent]
    [re-frame.core :refer [dispatch dispatch-sync]]
    [devtools.core :as devtools]
    [sixsq.slipstream.webui.utils :as utils]
    [taoensso.timbre :as timbre]

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
;; {:compiler-options {:closure-defines {'sixsq.slipstream.webui/HOST_URL false}}
;;
;; NOTE: When using an endpoint other than the one serving the javascript code
;; you MUST turn off the XSS protections of the browser.
;;
(goog-define HOST_URL "")
(def SLIPSTREAM_URL (delay (if-not (str/blank? HOST_URL) HOST_URL (utils/host-url))))

;;
;; hook to initialize the web application
;;
(defn ^:export init
  []
  (.log js/console "using slipstream server:" @SLIPSTREAM_URL)
  (dispatch-sync [:initialize-db])
  (dispatch-sync [:initialize-client @SLIPSTREAM_URL])
  (dispatch-sync [:fetch-cloud-entry-point])
  (dispatch-sync [:initialize-history])
  (reagent/render [sixsq.slipstream.webui.main.views/app]
                  (.getElementById js/document "container")))
