(ns sixsq.slipstream.webui.appstore.utils
  (:require [clojure.set :refer [map-invert]]))


(def next-steps {"data"        "credentials"
                 "credentials" "size"
                 "size"        "parameters"
                 "parameters"  "summary"
                 "summary"     nil})

(def previous-steps (map-invert next-steps))


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


