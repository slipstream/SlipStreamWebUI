(ns sixsq.slipstream.webui.data.utils
  (:require
    [sixsq.slipstream.webui.utils.time :as time]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [clojure.string :as str]
    [taoensso.timbre :as log]))


(defn matches-parameter-name?
  [parameter-name parameter]
  (and parameter-name (= parameter-name (:parameter parameter))))


(defn update-parameter-in-list
  [name value parameters]
  (let [f (partial matches-parameter-name? name)
        current (first (filter f parameters))               ;; FIXME: Use group-by instead?
        others (remove f parameters)]
    (if current
      (->> (assoc current :value value)
           (conj others)
           (sort-by :parameter)
           vec))))


(defn update-parameter-in-deployment
  [name value deployment]
  (->> deployment
       :module
       :content
       :inputParameters
       (update-parameter-in-list name value)
       (assoc-in deployment [:module :content :inputParameters])))


(defn create-time-period-filter
  [[time-start time-end]]
  (str "(data:timestamp>='"
       (time/time->utc-str time-start)
       "' and data:timestamp<'"
       (time/time->utc-str time-end)
       "')"))


(defn create-cloud-filter
  [credentials]
  (let [clouds (map (comp general-utils/resource-id->uuid :href :connector) credentials)]
    (some->> clouds
             (seq)
             (map #(str "connector/href='" % "'"))
             (str/join " or "))))


(defn join-filters
  [& filters]
  (->> filters
       (remove nil?)
       (map #(str "(" % ")"))
       (str/join " and ")))


