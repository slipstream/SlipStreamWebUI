(ns sixsq.slipstream.webui.components.column
  (:require
    [re-com.core :refer [h-box v-box label row-button]]
    [re-com.util :refer [deref-or-value]]
    [re-com.validate :refer-macros [validate-args-macro]]))

(def ^:const nbsp "\u00a0")

(defn column-header [header-class value on-remove]
  [h-box
   :justify :between
   :gap "1ex"
   :align :center
   :class header-class
   :children [[label :label (if (nil? value) nbsp value)]
              (when on-remove
                [row-button
                 :md-icon-name "zmdi zmdi-close"
                 :mouse-over-row? true
                 :on-click #(on-remove)])]])

(defn column-value [value-class key-fn value-fn entry]
  (let [k (key-fn entry)
        v (value-fn entry)]
    ^{:key k} [label
               :class value-class
               :label (if (nil? v) nbsp v)]))

(def column-args-desc
  [{:name        :model
    :required    true
    :type        "atom | collection"
    :description "collection of values or an atom containing a collection"}

   {:name        :key-fn
    :required    false
    :type        "key fn"
    :validate-fn #(or (fn? %) (keyword? %))
    :description "function to extract key value from element of collection, defaults to :key"}

   {:name        :value-fn
    :required    false
    :type        "value fn"
    :validate-fn #(or (fn? %) (keyword? %))
    :description "function to extract value from element of collection, defaults to :value"}

   {:name        :header
    :required    false
    :type        "string"
    :validate-fn string?
    :description "text for header of column"}

   {:name        :on-remove
    :required    false
    :type        "remove fn"
    :validate-fn #(or (fn? %) (keyword? %))
    :description "function called when the column is removed"}

   {:name        :class
    :required    false
    :type        "string"
    :validate-fn string?
    :description "class to use for the column as a whole (v-box)"}

   {:name        :header-class
    :required    false
    :type        "string"
    :validate-fn string?
    :description "class to use for the column header (label)"}

   {:name        :value-class
    :required    false
    :type        "string"
    :validate-fn string?
    :description "class to use for each value in the column (label)"}])

(defn column
  "Provides a column of values with an optional header."
  [& {:keys [model key-fn value-fn header on-remove class header-class value-class]
      :or   {key-fn :key, value-fn :value}
      :as   args}]
  {:pre [(validate-args-macro column-args-desc args "column")]}
  [v-box
   :class class
   :children [(when (or header on-remove)
                [column-header header-class header on-remove])
              (->> model
                   deref-or-value
                   (map (partial column-value value-class key-fn value-fn))
                   doall)]])
