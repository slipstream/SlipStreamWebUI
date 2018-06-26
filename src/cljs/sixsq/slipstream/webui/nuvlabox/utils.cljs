(ns sixsq.slipstream.webui.nuvlabox.utils
  (:require [sixsq.slipstream.client.api.cimi :as cimi]
            [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]))


(def default-params {:$first 1, :$last 20})

(def stale-nb-machines (assoc default-params :$filter "nextCheck < 'now'"))

(def active-nb-machines (assoc default-params :$filter "nextCheck >= 'now'"))


(defn nuvlabox-search
  [client params]
  (cimi/search client "nuvlaboxStates" (cimi-api-utils/sanitize-params params)))
