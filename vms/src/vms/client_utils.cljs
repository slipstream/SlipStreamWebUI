(ns vms.client-utils
  (:require [reagent.core :as reagent :refer [atom]]
            [sixsq.slipstream.client.async :as async-client]))

(defonce client (async-client/instance #_"https://localhost/api/cloud-entry-point" #_{:insecure? true}))

