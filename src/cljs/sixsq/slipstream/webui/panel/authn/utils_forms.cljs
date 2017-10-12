(ns sixsq.slipstream.webui.panel.authn.utils-forms
  (:require
    [re-com.core :refer [h-box v-box box input-text input-password label alert-box progress-bar
                         button info-button modal-panel single-dropdown title line gap throbber]]
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

(defn hidden? [{:keys [type] :as param-desc}]
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

(defn update-form-data
  [method param-name value]
  (dispatch [:evt.webui.authn/update-form-data [method param-name value]]))

(defn form-component
  "Provides a single element of a form. This should provide a reasonable
   control for each defined type, but this initial implementation just provides
   either a text or password field. The changed data is stored in the global
   database."
  [method [param-name {:keys [data type displayName] :as param}]]
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
                :on-change #(update-form-data method param-name %)]
    [input-text
     :attr {:name param-name}
     :width "100%"
     :model (reagent/atom (or data ""))
     :placeholder displayName
     :change-on-blur? true
     :on-change #(update-form-data method param-name %)]))
