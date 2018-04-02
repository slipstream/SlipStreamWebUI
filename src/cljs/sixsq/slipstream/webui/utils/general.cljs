(ns sixsq.slipstream.webui.utils.general
  (:require
    [cljs.tools.reader.edn :as edn]
    [clojure.string :as str]
    [clojure.set :as set]))


(defn str->int
  "Converts a string that contains a decimal representation of an integer into
   an integer. Returns nil for any invalid input."
  [s]
  (when (and (string? s)
             (re-find #"^-?(0|[1-9]\d*)$" s))
    (edn/read-string s)))


(defn prepare-params [params]
  (->> params
       (filter (fn [[k v]] (not (or (nil? v) (str/blank? v)))))
       (into {})))


(defn keys-in [m]
  (if (map? m)
    (vec
      (mapcat (fn [[k v]]
                (let [sub (keys-in v)
                      nested (map #(into [(name k)] %) (filter (comp not empty?) sub))]
                  (if (seq nested)
                    nested
                    [[(name k)]])))
              m))
    []))


(defn all-keys [m]
  (->> m
       keys-in
       (map #(str/join "/" %))
       set))


(defn merge-keys [coll]
  (->> coll
       (map #(dissoc % :acl :operations))
       (map all-keys)
       (reduce set/union)
       vec
       distinct
       sort
       vec))


(declare xml->json)


(defn node-name [node]
  (.-nodeName node))


(defn node-value [node]
  (.-nodeValue node))


(defn attributes [node]
  (for [i (range (.-attributes.length node))] (.attributes.item node i)))


(defn children [node]
  (for [i (range (.-childNodes.length node))] (.childNodes.item node i)))


(defn attribute-map [element]
  (if (and element (= 1 (.-nodeType element)))
    (into {} (map (juxt node-name node-value) (attributes element)))))


(defn unwrap-single-item [coll]
  (if (= 1 (count coll))
    (first coll)
    coll))


(defn blank-text-node? [[k v]]
  (and (= "#text" k) (str/blank? v)))


(defn child-map [element]
  (if (.hasChildNodes element)
    (->> (children element)
         (map (juxt node-name xml->json))
         (remove blank-text-node?)
         (group-by first)
         (map (fn [[k v]] [k (unwrap-single-item (map second v))]))
         (into {}))))


(defn xml->json [node]
  (if node
    (if (= (.-nodeType node) 3)
      (.-nodeValue node)                                    ;; text node
      (merge (attribute-map node) (child-map node)))))


(defn parse-resource-path
  "Utility to split a resource path into a vector of terms. Returns an empty
   vector for a nil argument. Removes blank or empty terms from the result."
  [path]
  (vec (remove str/blank? (str/split path #"/"))))


(defn truncate
  "Truncates a string to the given size and adds the optional suffix if the
   string was actually truncated."
  ([s]
    (truncate s 20 "\u2026"))
  ([s max-size]
    (truncate s max-size "\u2026"))
  ([s max-size suffix]
   (if (> (count s) max-size)
     (str (subs s 0 max-size) suffix)
     s)))


(defn edn->json [edn & {:keys [spaces] :or {spaces 2}}]
  (.stringify js/JSON (clj->js edn) nil spaces))


(defn json->edn [json & {:keys [keywordize-keys] :or {keywordize-keys true}}]
  (js->clj (.parse js/JSON json) :keywordize-keys keywordize-keys))


(defn random-id
  "Random six character string that can be used to generate unique
   identifiers."
  []
  (let [rand-alphanum #(rand-nth (vec "abcdefghijklmnopqrstuvwxyz0123456789"))]
    (str/join "" (take 6 (repeatedly rand-alphanum)))))
