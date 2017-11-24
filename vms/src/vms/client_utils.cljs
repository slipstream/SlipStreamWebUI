(ns vms.client-utils
  (:require [reagent.core :as reagent :refer [atom]]
            [sixsq.slipstream.client.async :as async-client]))

(defonce client (async-client/instance "https://localhost/api/cloud-entry-point" {:insecure? true}))

