(ns sixsq.slipstream.webui.utils.form-fields
  (:require
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.utils.general :as utils]))


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
                             consumerMandatory consumerWritable] :as attribute}]
  ^{:key name}
  [ui/FormField {:required consumerMandatory}
   (when-not hidden [:label displayName nbsp (help-popup help)])
   [ui/Input
    (cond-> {:type          (if sensitive "password" "text")
             :name          (or displayName name)
             :default-value (or (:value vscope) (:default vscope) "")
             :read-only     (not consumerWritable)
             :on-change     (ui-callback/value #(update-fn form-id name %))}
            hidden (assoc :style {:display "none"}))]])


(defmethod form-field :integer
  [update-fn form-id {:keys [name displayName help hidden vscope
                             consumerMandatory consumerWritable] :as attribute}]
  ^{:key name}
  [ui/FormField {:required consumerMandatory}
   (when-not hidden [:label displayName nbsp (help-popup help)])
   [ui/Input
    (cond-> {:type          "number"
             :name          (or displayName name)
             :default-value (or (:value vscope) (:default vscope) "")
             :read-only     (not consumerWritable)
             :on-change     (ui-callback/value #(update-fn form-id name (utils/str->int %)))}
            hidden (assoc :style {:display "none"}))]])


(defmethod form-field :boolean
  [update-fn form-id {:keys [name displayName help hidden vscope
                             consumerMandatory consumerWritable] :as attribute}]
  ^{:key name}
  [ui/FormField {:required consumerMandatory}
   (when-not hidden [:label displayName nbsp (help-popup help)])
   [ui/Checkbox
    (cond-> {:type          "number"
             :name          (or displayName name)
             :default-value (or (:value vscope) (:default vscope) false)
             :read-only     (not consumerWritable)
             :on-change     (ui-callback/checked #(update-fn form-id name %))}
            hidden (assoc :style {:display "none"}))]])


(defmethod form-field :ref
  [update-fn form-id {:keys [name displayName help hidden vscope
                             consumerMandatory consumerWritable] :as attribute}]
  ^{:key name}
  [ui/FormField {:required consumerMandatory}
   (when-not hidden [:label displayName nbsp (help-popup help)])
   [ui/Checkbox
    (cond-> {:name          (or displayName name)
             :default-value (or (:value vscope) (:default vscope) "")
             :read-only     (not consumerWritable)
             :on-change     (ui-callback/value #(update-fn form-id name {:href %}))}
            hidden (assoc :style {:display "none"}))]])

;
;(defmethod form-field :enum
;  [update-fn form-id [param-name {:keys [enum data displayName mandatory description]}]]
;  ^{:key displayName}
;  [ui/FormField {:required mandatory}
;   [:label displayName nbsp (help-popup description)]
;   [ui/FormSelect
;    {:options   (mapv (fn [v] {:value v, :text v}) enum)
;     :value     data
;     :on-change (ui-callback/value #(update-fn form-id param-name %))}]])

; add support for following types map, array, dateTime