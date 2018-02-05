(ns sixsq.slipstream.webui.cimi.utils)


(defn collection-href-map
  "Creates a map from the CloudEntryPoint that maps the resource collection
   key (as a keyword) to the href for the collection (as a string)."
  [cep]
  (when cep
    (into {} (->> cep
                  (map (juxt first #(:href (second %))))
                  (remove (fn [[k v]] (not (string? v))))))))


(defn collection-key-map
  "Creates a map from the CloudEntryPoint that maps the collections href (as a
   string) to the key for the collection (as a keyword)."
  [cep]
  (when cep
    (into {} (->> cep
                  collection-href-map
                  (map (juxt second first))))))

(defn template-resource-key
  "Returns the collection keyword for the template resource associated with
   the given collection. If there is no template resource, then nil is
   returned."
  [cloud-entry-point collection-href]
  (when-let [href->key (:collection-key cloud-entry-point)]
    (href->key (str collection-href "-template"))))