(ns sixsq.slipstream.webui.utils-forms
  (:require
    [re-com.core :refer [h-box v-box box input-text input-password label alert-box scroller
                         button info-button modal-panel single-dropdown title line gap checkbox]]
    [re-com.util :refer [deref-or-value]]
    [re-com.validate :refer-macros [validate-args-macro]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [sixsq.slipstream.webui.panel.authn.views-session :as session]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils :as utils]
    [taoensso.timbre :as log]))

(defn hidden?
  "Determines if the given parameter description describes a hidden field."
  [{:keys [type] :as param-desc}]
  (= "hidden" type))

(defn ordered-params
  "Extracts and orders the parameter descriptions for rendering the form.
   Returns a tuple with two ordered parameter groups. The first contains the
   list of hidden parameters; the second contains the list of visible ones."
  [method]
  (let [params (->> method
                    :params-desc
                    seq
                    (sort-by (fn [[_ {:keys [order]}]] order))
                    (group-by (fn [[k v]] (hidden? v))))]
    [(get params true) (get params false)]))

(defn form-component
  "Provides a single element of a form. This should provide a reasonable
   control for each defined type, but this initial implementation just provides
   either a text or password field. You must provide an update-form-data-fn that
   takes three arguments the form ID, parameter name, and parameter value."
  [update-form-data-fn form-id [param-name {:keys [data type displayName] :as param}]]
  (case type
    "hidden" [:input {:name  param-name
                      :type  "hidden"
                      :value (or data "")}]
    "password" [input-password
                :attr {:name param-name}
                :width "100%"
                :model (reagent/atom (or data ""))
                :placeholder displayName
                :change-on-blur? true
                :on-change #(update-form-data-fn form-id param-name %)]
    "int" [input-text
           :attr {:name param-name}
           :width "100%"
           :model (reagent/atom (or data ""))
           :placeholder displayName
           :change-on-blur? true
           :on-change #(update-form-data-fn form-id param-name (utils/str->int %))]
    "boolean" [checkbox
               :model (reagent/atom false)
               :label displayName
               :on-change #(update-form-data-fn form-id param-name (boolean %))]
    [input-text
     :attr {:name param-name}
     :width "100%"
     :model (reagent/atom (or data ""))
     :placeholder displayName
     :change-on-blur? true
     :on-change #(update-form-data-fn form-id param-name %)]))

(defn update-data [form-data-atom form-id param value]
  (let [data (cond-> {:href form-id}
                     param (merge {param value}))]
    (if (= form-id (:href @form-data-atom))
      (swap! form-data-atom merge data)
      (reset! form-data-atom data))))

(defn credential-template-form
  [form-data-atom {:keys [id] :as description}]
  (let [[hidden-params visible-params] (ordered-params description)
        update-data-fn (partial update-data form-data-atom)
        form-component-fn (partial form-component update-data-fn id)]
    [scroller
     :max-height "25ex"
     :v-scroll :auto
     :h-scroll :off
     :child [v-box
             :children [(when (pos? (count hidden-params))
                          [v-box
                           :children (vec (map form-component-fn hidden-params))])
                        (when (pos? (count visible-params))
                          [v-box
                           :gap "0.5ex"
                           :children (vec (map form-component-fn visible-params))])]]]))

(defn credential-template-selector
  [update-form-data-fn selected-id-atom descriptions]
  [single-dropdown
   :choices descriptions
   :model selected-id-atom
   :on-change (fn [new-id]
                (reset! selected-id-atom new-id)
                (update-form-data-fn new-id nil nil))
   :placeholder "resource template"
   :style {:min-width "40ex"}])

(defn action-buttons
  [form-data-atom on-submit-fn on-cancel-fn]
  [h-box
   :justify :between
   :children [[button
               :class "btn btn-default"
               :label "cancel"
               :on-click on-cancel-fn]
              [button
               :class "btn btn-primary"
               :label "create"
               :on-click #(on-submit-fn @form-data-atom)]]])

(defn credential-form-container-inner
  [descriptions-atom on-submit on-cancel]
  (let [selected-id (reagent/atom nil)
        form-data (reagent/atom nil)]
    (fn []
      (let [descriptions @descriptions-atom]
        [v-box
         :width "40ex"
         :gap "2ex"
         :children [[credential-template-selector
                     (partial update-data form-data)
                     selected-id
                     descriptions]
                    [credential-template-form form-data (first (filter #(= @selected-id (:id %)) descriptions))]
                    [action-buttons form-data on-submit on-cancel]]]))))

;;
;; public component
;;

(def form-args-desc
  [{:name        :descriptions
    :required    true
    :type        "vector of parameter descriptions | atom"
    :description "vector or atom that sequence of parameter descriptions"}

   {:name        :on-submit
    :required    true
    :type        "function called with form data on submit"
    :validate-fn fn?
    :description "1-arg (form data map) function called on submit"}

   {:name        :on-cancel
    :required    true
    :type        "function called when cancel button is pressed"
    :validate-fn fn?
    :description "0-arg function called on cancel"}
   ])

(defn credential-form-container
  [& {:keys [descriptions on-submit on-cancel]
      :or   {on-submit #()
             on-cancel #()}
      :as   args}]
  {:pre [(validate-args-macro form-args-desc args "form")]}
  [credential-form-container-inner descriptions on-submit on-cancel])

