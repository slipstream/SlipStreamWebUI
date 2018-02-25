(ns sixsq.slipstream.webui.deployment-detail.utils
  (:require
    [cljs-time.format :as time-fmt]
    [cljs-time.coerce :as time-coerce]))


(defn parse-timestamp
  "Returns a date-time instance from a string containing a time in ISO8201
   format. Returns nil on any exception."
  [timestamp]
  (try
    (let [formatter (time-fmt/formatters :date-time)]
      (time-fmt/parse formatter timestamp))
    (catch js/Error _
      nil)))


(defn delta-time-mins
  "Returns the difference in the given date-time instances in minutes (with 1
   decimal place). Note that the implementation doesn't create a time/internal
   because the function doesn't appear to work correctly."
  [start end]
  (let [start-ms (time-coerce/to-long start)
        end-ms (time-coerce/to-long end)
        delta-mins (/ (int (/ (- end-ms start-ms) 6000.)) 10.)]
    delta-mins))


(defn assoc-delta-time
  "Given the start (as a string), this adds a :delta-time entry in minutes."
  [start {end :timestamp :as evt}]
  (let [start-date-time (parse-timestamp start)
        end-date-time (parse-timestamp end)]
    (assoc evt :delta-time (delta-time-mins start end))))


