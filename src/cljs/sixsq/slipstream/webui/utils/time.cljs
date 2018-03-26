(ns sixsq.slipstream.webui.utils.time
  (:require
    [cljsjs.moment]
    [clojure.string :as str]))


(def ^:const default-locale "en")


(def ^:private iso8601-format (.-ISO_8601 js/moment))


(def ^:private rfc2822-format (.-RFC_2822 js/moment))


(defn n-decimal-places
  ([decimal-string]
   (n-decimal-places decimal-string 2))
  ([decimal-string n]
   (let [[left right] (str/split decimal-string #"\.")]
     (str left "." (apply str (take n (concat right (repeat "0"))))))))


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


(defn parse-rfc2822
  ([rfc2822]
   (parse-rfc2822 rfc2822 default-locale))
  ([rfc2822 locale]
   (js/moment rfc2822 rfc2822-format locale true)))


(defn invalid
  ([]
   (invalid default-locale))
  ([locale]
   (-> js/moment
       .invalid
       (.locale locale)
       .format)))


(defn ago
  "Returns a human-readable string on how much time is remaining before the
   given expiry date (in RFC 2822 format). Uses English as the natural language
   unless another locale is given."
  ([moment]
   (ago moment default-locale))
  ([moment locale]
   (if-let [localized-moment (some-> moment
                                     .clone
                                     (.locale locale))]
     (.fromNow localized-moment)
     (invalid locale))))


(defn remaining
  "Returns a human-readable string on how much time is remaining before the
   given expiry date (in RFC 2822 format). Uses English as the natural language
   unless another locale is given."
  ([expiry-rfc2822]
   (remaining expiry-rfc2822 default-locale))
  ([expiry-rfc2822 locale]
   (if expiry-rfc2822
     (-> expiry-rfc2822
         (parse-rfc2822)
         (.locale locale)
         (.toNow true))
     (invalid locale))))


(defn delta-minutes
  "Returns the difference in the given date-time instances in minutes (with 2
   decimal places)."
  ([start]
   (delta-minutes start (now)))
  ([start end]
   (let [start-moment (parse-iso8601 start)
         end-moment (parse-iso8601 end)]
     (n-decimal-places (.diff end-moment start-moment "minutes" true)))))


(defn days-before
  ([n]
   (days-before n default-locale))
  ([n locale]
   (-> (now locale) (.startOf "date") (.add (- n) "days"))))
