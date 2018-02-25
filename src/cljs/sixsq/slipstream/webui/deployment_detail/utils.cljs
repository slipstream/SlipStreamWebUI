(ns sixsq.slipstream.webui.deployment-detail.utils
  (:require
    [cljs-time.format :as time-fmt]
    [cljs-time.coerce :as time-coerce]))


(defn millis-to-minutes
  "Returns a double containing the number of minutes based on the given number
   of milliseconds."
  [millis]
  (/ millis 60000.))


(defn trailing-zero
  "Adds a trailing zero if the string ends with a decimal point."
  [s]
  (if (re-matches #".*\.$" s)
    (str s "0")
    s))


(defn format-minutes
  "Returns a string representation of the argument with minutes rounded to 1
   decimal place."
  [minutes]
  (-> minutes
      double
      (* 10.)
      int
      (/ 10.)
      str
      trailing-zero))


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
        delta-mins (format-minutes (millis-to-minutes (- end-ms start-ms)))]
    delta-mins))


(defn assoc-delta-time
  "Given the start (as a string), this adds a :delta-time entry in minutes as
   a formatted string."
  [start {end :timestamp :as evt}]
  (assoc evt :delta-time (delta-time-mins start end)))




