(ns sixsq.slipstream.webui.core
  (:require
    [reagent.core :as r]
    [re-frame.core :refer [clear-subscription-cache! dispatch-sync dispatch]]
    [sixsq.slipstream.webui.config :as config]
    [sixsq.slipstream.webui.routes :as routes]
    [sixsq.slipstream.webui.utils.defines :as defines]
    [sixsq.slipstream.webui.authn.events :as authn-events]
    [sixsq.slipstream.webui.cimi.events :as cimi-events]
    [sixsq.slipstream.webui.client.events :as client-events]
    [sixsq.slipstream.webui.dashboard.events :as dashboard-events]
    [sixsq.slipstream.webui.db.events :as db-events]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.main.views :as main-views]
    [sixsq.slipstream.webui.authn.views :as authn-views]
    [sixsq.slipstream.webui.utils.general :as utils]
    [taoensso.timbre :as log]
    [clojure.string :as str]))

;;
;; determine the host url
;;
(def SLIPSTREAM_URL (delay (if-not (str/blank? defines/HOST_URL) defines/HOST_URL (utils/host-url))))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (log/info "development mode")))

(defn render-component-when-present
  ([tag comp & {:keys [initialization-fn]}]
   (when-let [container-element (.getElementById js/document tag)]
     (log/info "Rendering " tag)
     (when initialization-fn (initialization-fn))
     (r/render [comp] container-element))))

(defn mount-root []
  (clear-subscription-cache!)
  (render-component-when-present "app" main-views/app)
  (render-component-when-present
    "modal-login" authn-views/modal-login
    :initialization-fn #(do (dispatch-sync [::authn-events/server-redirect-uri "/login"])
                            (dispatch-sync [::authn-events/redirect-uri "/dashboard"]))))

(defn ^:export open-modal []
  (log/debug "dispatch open-modal for authn view")
  (dispatch [::authn-events/open-modal]))

(defn ^:export init []
  (routes/routes)
  (dispatch-sync [::db-events/initialize-db])
  (dispatch-sync [::client-events/initialize @SLIPSTREAM_URL])
  (dispatch-sync [::history-events/initialize @config/path-prefix])
  (dispatch-sync [::authn-events/initialize])
  (dispatch-sync [::cimi-events/get-cloud-entry-point])
  (log/info "finished initialization")
  (dev-setup)
  (mount-root))
