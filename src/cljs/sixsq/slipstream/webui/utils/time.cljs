(ns sixsq.slipstream.webui.utils.time
  (:require
    [cljsjs.moment]
    [clojure.string :as str]))


(def ^:const default-locale "en")


(def ^:private iso8601-format (.-ISO_8601 js/moment))


(defn now
  ([]
   (now default-locale))
  ([locale]
   (.locale (js/moment) locale)))


(defn parse-iso8601
  ([iso8601]
   (parse-iso8601 iso8601 default-locale))
  ([iso8601 locale]
   (js/moment iso8601 iso8601-format locale true)))


(defn invalid
  ([]
   (invalid default-locale))
  ([locale]
   (-> js/moment .invalid .clone (.locale locale) .format)))


(defn ago
  "Returns a human-readable string on how much time is remaining before the
   given expiry date (as a moment object). Uses English as the natural language
   unless another locale is given."
  ([moment]
   (ago moment default-locale))
  ([moment locale]
   (or (some-> moment .clone (.locale locale) .fromNow)
       (invalid locale))))


(defn before-now?
  [iso8601]
  (let [ts (parse-iso8601 iso8601)]
    (boolean (.isBefore ts (now)))))


(defn after-now?
  [iso8601]
  (let [ts (parse-iso8601 iso8601)]
    (boolean (.isAfter ts (now)))))


(defn remaining
  "Returns a human-readable string on how much time is remaining before the
   given expiry date (in ISO8601 format). Uses English as the natural language
   unless another locale is given."
  ([expiry-iso8601]
   (remaining expiry-iso8601 default-locale))
  ([expiry-iso8601 locale]
   (or (some-> expiry-iso8601 (parse-iso8601) .clone (.locale locale) (.toNow true))
       (invalid locale))))

(defn delta-duration
  ([start]
   (delta-duration start (now)))
  ([start end]
   (let [start-moment (parse-iso8601 start)
         end-moment (parse-iso8601 end)]
     (.duration js/moment (.diff end-moment start-moment true)))))

(defn delta-minutes
  "Returns the difference in the given date-time instances in minutes."
  ([start]
   (delta-minutes start (now)))
  ([start end]
   (.asMinutes (delta-duration start end))))

(defn delta-milliseconds
  "Returns the difference in the given date-time instances in milliseconds."
  ([start]
   (delta-milliseconds start (now)))
  ([start end]
   (.asMilliseconds (delta-duration start end))))


(defn days-before
  ([n]
   (days-before n default-locale))
  ([n locale]
   (-> (now locale) (.startOf "date") (.add (- n) "days"))))


(defn time-value
  [iso8601]
  (str (-> iso8601 parse-iso8601 ago) " (" iso8601 ")"))
