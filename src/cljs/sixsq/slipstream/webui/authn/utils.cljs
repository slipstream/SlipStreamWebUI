(ns sixsq.slipstream.webui.authn.utils
  (:require
    [clojure.string :as str]))


(defn has-role? [session role]
  (some-> session :roles (str/split #"\s+") set (contains? role)))


(defn method-comparator
  "Compares two login method types. The value 'internal' will always compare
   as less than anything other than itself."
  [x y]
  (cond
    (= x y) 0
    (= "internal" x) -1
    (= "internal" y) 1
    (< x y) -1
    :else 1))


(defn sort-value [[tag [{:keys [method]}]]]
  (if (= "internal" method)
    "internal"
    (or tag method)))


(defn order-and-group
  "Sorts the methods by ID and then groups them (true/false) on whether it is
   an internal method or not."
  [methods]
  (->> methods
       (sort-by :id)
       (group-by #(or (:group %) (:method %)))
       (sort-by sort-value method-comparator)))


(defn internal-or-api-key
  [[_ methods]]
  (let [authn-method (:method (first methods))]
    (#{"internal" "api-key"} authn-method)))


(defn self-registration
  [[_ methods]]
  (let [authn-method (:method (first methods))]
    (#{"self-registration"} authn-method)))


(defn hidden? [{:keys [type] :as param-desc}]
  (= "hidden" type))


(defn ordered-params
  "Extracts and orders the parameter descriptions for rendering the form."
  [method]
  (->> method
       :params-desc
       seq
       (sort-by (fn [[_ {:keys [order]}]] order))))


(defn keep-visible-params
  "Keeps the form parameters that should be shown to the user. It removes all
   readOnly parameters along with :name and :description."
  [[k {:keys [readOnly]}]]
  (and (not= :name k)
       (not= :description k)
       (not= :group k)
       (not= :redirectURI k)
       (not readOnly)))


(defn select-method-by-id
  [id methods]
  (->> methods
       (filter #(= id (:id %)))
       first))


(defn select-group-methods-by-id
  [id method-groups]
  (->> method-groups
       (filter #(-> % first (= id)))
       first
       second))


(defn grouped-authn-methods
  "Takes a set of raw downloaded templates, groups and orders them by the
   :group (or :method) value. Returns a vector of tuples [group-key
   authn-methods]."
  [templates classifier-fn]
  (let [method-groups (-> templates :templates vals order-and-group)
        internals (filter classifier-fn method-groups)
        externals (remove classifier-fn method-groups)]
    (vec (concat internals externals))))
