(ns sixsq.slipstream.webui
  (:require
   [reagent.core :as reagent]
   [re-frame.core :refer [dispatch dispatch-sync]]
   [devtools.core :as devtools]

   [sixsq.slipstream.webui.main.events]
   [sixsq.slipstream.webui.main.subs]
   [sixsq.slipstream.webui.main.views]))

;;
;; debugging tools
;;
(devtools/install!)
(enable-console-print!)

;;
;; hook to initialize the web application
;;
(defn ^:export init
  []
  (dispatch-sync [:initialize-db])
  (dispatch-sync [:initialize-client])
  (dispatch-sync [:fetch-cloud-entry-point])
  (reagent/render [sixsq.slipstream.webui.main.views/app]
                  (.getElementById js/document "container")))
