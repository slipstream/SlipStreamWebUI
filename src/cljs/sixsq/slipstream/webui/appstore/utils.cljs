(ns sixsq.slipstream.webui.appstore.utils)


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


(defn update-parameter-in-template
  [name value template]
  (->> template
       :module
       :content
       :inputParameters
       (update-parameter-in-list name value)
       (assoc-in template [:module :content :inputParameters])))


