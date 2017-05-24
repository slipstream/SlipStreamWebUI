(ns sixsq.slipstream.webui.utils
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
  (let [filter-value (:$filter params)]
    (if (or (nil? filter-value) (str/blank? filter-value))
      (dissoc params :$filter)
      params)))

(defn coerce-pos-int [s]
  (when-let [v (str->int s)]
    (when (pos? v)
      v)))

(defn coerse-filter [s]
  (when-not (str/blank? s)
    s))

(defn remove-nil-vals
  [m]
  (into {} (remove (comp nil? second) m)))

(defn merge-offer-params [params url-params]
  {:$first (or (str->int (:$first url-params)) (:$first params))
   :$last (or (str->int (:$last url-params)) (:$last params))
   :$filter (or (:$filter url-params) (:$filter params))})

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
       (map all-keys)
       (reduce set/union)
       vec
       sort
       (map (fn [v] {:id v :label v}))))

(defn id->path [id]
  (map keyword (str/split id #"/")))

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

(defn host-url
  "Extracts the host URL from the javascript window.location object."
  []
  (if-let [location (.-location js/window)]
    (let [protocol (.-protocol location)
          host (.-hostname location)
          port (.-port location)
          port-field (when-not (str/blank? port) (str ":" port))]
      (str protocol "//" host port-field))))

(defn parse-resource-path
  "Utility to split a resource path into a vector of terms. Returns an empty
   vector for a nil argument. Removes blank or empty terms from the result."
  [path]
  (vec (remove str/blank? (str/split path #"/"))))

(defn truncate
  "Truncates a string to the given size and adds the optional suffix if the
   string was actually truncated."
  [s max-size & [suffix]]
  (if (> (count s) max-size)
    (str (str/join "" (take max-size s)) suffix)
    s))

