;; FIXME: Copied namespace com.sixsq.slipstream.ssclj.util.spec.  Refactor common server/client utilities.
(ns sixsq.slipstream.webui.db
  "Utilities that provide common spec definition patterns that aren't
   supported directly by the core spec functions and macros."
  (:require
    [clojure.set :as set]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.string :as str]))

(def ^:private all-ascii-chars (map str (map char (range 0 256))))

(defn- regex-chars
  "Provides a list of ASCII characters that satisfy the regex pattern."
  [pattern]
  (set (filter #(re-matches pattern %) all-ascii-chars)))

(defn merge-kw-lists
  "Merges two lists (or seqs) of namespaced keywords. The results will
   be a sorted vector with duplicates removed."
  [kws1 kws2]
  (vec (sort (set/union (set kws1) (set kws2)))))

(defn merge-keys-specs
  "Merges the given clojure.spec/keys specs provided as a list of maps.
   All the arguments are eval'ed and must evaluate to map constants."
  [map-specs]
  (->> map-specs
       (map eval)
       (apply merge-with merge-kw-lists)))

(defn unnamespaced-kws
  "Removes the namespaces from the provided list of keywords
   and returns the resulting set."
  [kws]
  (set (map (comp keyword name) kws)))

(defn allowed-keys
  "Returns a set of all the allowed keys from a clojure.spec/keys
   specification provided as a map."
  [{:keys [req req-un opt opt-un]}]
  (set (concat req
               (unnamespaced-kws req-un)
               opt
               (unnamespaced-kws opt-un))))

(defmacro regex-string
  "Creates a string spec that matches the given regex with a generator
   that randomly selects from the ASCII characters identified by the
   char-pattern."
  [char-pattern regex]
  (let [allowed-chars (regex-chars char-pattern)]
    `(s/with-gen (s/and string? #(re-matches ~regex %))
                 (constantly (gen/fmap str/join (gen/vector (s/gen ~allowed-chars)))))))

(defmacro only-keys
  "Creates a closed map definition where only the defined keys are
   permitted. The arguments must be literals, using the same function
   signature as clojure.spec/keys.  (This implementation was provided
   on the clojure mailing list by Alistair Roche.)"
  [& {:as kw-args}]
  `(s/merge (s/keys ~@(apply concat (vec kw-args)))
            (s/map-of ~(allowed-keys kw-args) any?)))

(defmacro only-keys-maps
  "Creates a closed map definition from one or more maps that contain
   key specifications as for clojure.spec/keys. All of the arguments
   are eval'ed, so they may be vars containing the definition(s). All
   of the arguments must evaluate to compile-time map constants."
  [& map-specs]
  (let [map-spec (merge-keys-specs map-specs)]
    `(s/merge (s/keys ~@(apply concat (vec map-spec)))
              (s/map-of ~(allowed-keys map-spec) any?))))

(defmacro constrained-map
  "Creates an open map spec using the supplied keys specs with the
   additional contraint that all unspecified entries must match the
   given key and value specs. The keys specs will be evaluated."
  [key-spec value-spec & map-specs]
  (let [map-spec (merge-keys-specs map-specs)]
    `(s/merge
       (s/every (s/or :attrs (s/tuple ~(allowed-keys map-spec) any?)
                      :link (s/tuple ~key-spec ~value-spec)))
       (s/keys ~@(apply concat (vec map-spec))))))
