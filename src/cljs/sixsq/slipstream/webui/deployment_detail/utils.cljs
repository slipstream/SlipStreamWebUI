(ns sixsq.slipstream.webui.deployment-detail.utils
  (:require
    [sixsq.slipstream.webui.utils.time :as time]))


(defn assoc-delta-time
  "Given the start (as a string), this adds a :delta-time entry in minutes."
  [start {end :timestamp :as evt}]
  (assoc evt :delta-time (time/delta-minutes start end)))

