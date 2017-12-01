(ns sixsq.slipstream.dashboard-tabs.utils.client
  (:require
    [clojure.string :as str]
    [taoensso.timbre :as log]
    [sixsq.slipstream.client.async :as async-client]))

(defn host-url
      "Extracts the host URL from the javascript window.location object."
      []
      (if-let [location (.-location js/window)]
              (let [protocol (.-protocol location)
                    host (.-hostname location)
                    port (.-port location)
                    port-field (when-not (str/blank? port) (str ":" port))]
                   (str protocol "//" host port-field))))


;;
;; determine the host url
;;
(def SLIPSTREAM_URL (delay (host-url)))

(log/info "using slipstream server:" @SLIPSTREAM_URL)

(def client (async-client/instance
              (str @SLIPSTREAM_URL "/api/cloud-entry-point")
              ;"https://localhost/api/cloud-entry-point" {:insecure? true} ;dev
              ;nil   ; nuv.la
              ))
