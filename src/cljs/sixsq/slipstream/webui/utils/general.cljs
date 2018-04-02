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


(defn parse-resource-path
  "Utility to split a resource path into a vector of terms. Returns an empty
   vector for a nil argument. Removes blank or nil terms from the result."
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


(def rand-alphanums (repeatedly #(rand-nth (vec "abcdefghijklmnopqrstuvwxyz0123456789"))))


(defn random-element-id
  "Random character string that can be used to generate unique element
   identifiers. By default, will produce a string with 6 characters."
  ([]
   (random-element-id 6))
  ([n]
   (str/join "" (take n rand-alphanums))))
