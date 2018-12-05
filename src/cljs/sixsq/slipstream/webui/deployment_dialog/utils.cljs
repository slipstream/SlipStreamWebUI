(ns sixsq.slipstream.webui.deployment-dialog.utils)


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


(defn params-to-remove-fn
  "Creates a function (based on the credential) to identify input parameters
   that should be removed from the UI."
  [selected-credential]
  (let [is-not-docker? (not= (:type selected-credential) "cloud-cred-docker")]
    (cond-> #{"credential.id"}
            is-not-docker? (conj "cloud.node.publish.ports"))))


(defn remove-input-params
  "Removes the input parameters from the collection that are identified by the
   remove-param? function."
  [collection remove-param?]
  (remove #(remove-param? (:parameter %)) collection))



(defn service-offers->mounts
  [service-offers]
  (->> service-offers
       :serviceOffers
       (map (keyword "resource:bucket"))
       distinct
       (map #(str "type=volume,src=" % ",dst=/mnt/" % ",readonly"))))
