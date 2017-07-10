(ns sixsq.slipstream.webui.panel.cimi.utils)

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
