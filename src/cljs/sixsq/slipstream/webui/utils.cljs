(ns sixsq.slipstream.webui.utils
  (:require
    [cljs.tools.reader.edn :as edn]
    [clojure.string :as str]
    [clojure.set :as set]))

(defn str->int
  [s]
  (when (and (string? s)
             (re-find #"^\d+$" s))
    (edn/read-string s)))

(defn prepare-params [params]
  (let [filter-value (:$filter params)]
    (if (or (nil? filter-value) (str/blank? filter-value))
      (dissoc params :$filter)
      params)))

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


