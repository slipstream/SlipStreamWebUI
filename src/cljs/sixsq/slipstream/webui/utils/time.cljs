(ns sixsq.slipstream.webui.utils.time
  (:require
    [cljsjs.moment]
    [clojure.string :as str]))

(def ^:private iso8601-format (.-ISO_8601 js/moment))


(def ^:private rfc2822-format (.-RFC_2822 js/moment))


(defn now
  []
  (js/moment))


(defn parse-iso8601
  [iso8601]
  (.parse js/moment iso8601 iso8601-format "en" true))


(defn parse-rfc2822
  [rfc2822]
  (.parse js/moment rfc2822 rfc2822-format "en" true))


(defn remaining
  "Returns a human-readable string on how much time is remaining before the
   given expiry date (in ISO 8601 format)."
  [expiry-rfc2822]
  (-> (or expiry-rfc2822 "invalid-time")
      (str/replace "UTC" "Z")
      parse-rfc2822
      (.toNow true)))


(defn delta-minutes
  "Returns the difference in the given date-time instances in minutes (with 1
   decimal place). Note that the implementation doesn't create a time/internal
   because the function doesn't appear to work correctly."
  ([start]
   (delta-minutes start (now)))
  ([start end]
   (let [start-moment (parse-iso8601 start)
         end-moment (parse-iso8601 end)]
     (.diff end-moment start-moment "minutes" true))))
