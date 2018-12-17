(ns sixsq.slipstream.webui.utils.form-fields-resource-metadata
  (:require
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]))


(def nbsp "\u00a0")


(defn help-popup [description]
  (when description
    (let [icon [ui/Icon {:name "help circle"}]]
      [ui/Popup
       {:trigger        (reagent/as-element icon)
        :content        description
        :on             "hover"
        :hide-on-scroll true}])))


(defmulti form-field
          (fn [update-fn form-id {:keys [type] :as param}]
            (keyword type)))


(defmethod form-field :default
  [update-fn form-id {:keys [name displayName help hidden sensitive vscope
                             consumerMandatory consumerWritable] :as param}]
  ^{:key displayName}
  [ui/FormField {:required consumerMandatory}
   [:label displayName nbsp (help-popup help)]
   [ui/Input
    {:type      (cond
                  sensitive "password"
                  hidden "hidden"
                  :else "text")
     :name      (or displayName name)
     :default-value (or (:value vscope) (:default vscope) "")
     :read-only (not consumerWritable)
     :fluid     true
     :on-change (ui-callback/value #(update-fn form-id name %))}]])
