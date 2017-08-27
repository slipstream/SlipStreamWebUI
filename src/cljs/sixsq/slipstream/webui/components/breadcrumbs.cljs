(ns sixsq.slipstream.webui.components.breadcrumbs
  (:require
    [re-com.core :refer [h-box label hyperlink gap]]
    [re-com.validate :refer-macros [validate-args-macro]]))

(defn format-crumb-hyperlink
  [model on-change class index]
  (let [label (str (get @model index))
        trim-value (subvec @model 0 (inc index))]
    [hyperlink
     :label label
     :disabled? (= (count @model) (inc index))
     :class (or class "")
     :on-click (fn []
                 (on-change trim-value))]))

(def breadcrumbs-args-desc
  [{:name        :model
    :required    true
    :type        "atom"
    :description "atom containing a vector of strings, the breadcrumbs"}

   {:name        :on-change
    :required    true
    :type        "update fn"
    :validate-fn fn?
    :description "1-arg (new breadcrumbs vector) function called on changes"}

   {:name        :class
    :required    false
    :type        "string"
    :validate-fn string?
    :description "class to use for the breadcrumb container (h-box)"}

   {:name        :separator
    :required    false
    :type        "string"
    :validate-fn string?
    :description "separator to use, default is centered dot"}

   {:name        :separator-class
    :required    false
    :type        "string"
    :validate-fn string?
    :description "class to use for the separator (label)"}

   {:name        :breadcrumb-class
    :required    false
    :type        "string"
    :validate-fn string?
    :description "class to use a single breadcrumb (hyperlink)"}])

(defn breadcrumbs
  "Provides a sequence of clickable breadcrumbs with a separator
   between them.

   The model must be an atom containing a vector of the breadcrumbs to
   render.

   The 1-arg on-change function will receive the updated breadcrumbs
   when changes are made.

   The separator, if given, will be used.  If not, the empty string is
   used.

   The class arguments (class, separator-class, crumb-class) can be used
   to style the various breadcrumb elements from CSS."
  [& {:keys [model on-change class separator separator-class breadcrumb-class]
      :as   args}]
  {:pre [(validate-args-macro breadcrumbs-args-desc args "breadcrumbs")]}
  (let [separator-label [label
                         :label (or separator "")
                         :class (or separator-class "")]
        link-fn (partial format-crumb-hyperlink model on-change breadcrumb-class)
        crumbs (map link-fn (range (count @model)))]
    [h-box
     :class (or class "")
     :gap "0.5ex"
     :children (interpose separator-label crumbs)]))
