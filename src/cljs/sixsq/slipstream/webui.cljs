(ns sixsq.slipstream.webui
  (:require
   [reagent.core :as reagent]
   [re-frame.core :refer [dispatch dispatch-sync]]
   [devtools.core :as devtools]

   [sixsq.slipstream.webui.effects]
   [sixsq.slipstream.webui.events]
   [sixsq.slipstream.webui.subs]
   [sixsq.slipstream.webui.views]))

(enable-console-print!)

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
  (reagent/render [sixsq.slipstream.webui.views/app]
                  (.getElementById js/document "container")))
