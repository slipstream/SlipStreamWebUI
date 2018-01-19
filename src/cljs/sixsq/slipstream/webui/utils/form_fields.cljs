(ns sixsq.slipstream.webui.utils.form-fields
  (:require
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.component :as ui-utils]
    [sixsq.slipstream.webui.utils.general :as utils]
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
          (fn [update-fn form-id [param-name {:keys [type] :as param}]]
            (keyword type)))


(defmethod form-field :default
  [update-fn form-id [param-name {:keys [data displayName readOnly mandatory description] :as param}]]
  (log/error (with-out-str (cljs.pprint/pprint param)))
  ^{:key displayName}
  [ui/FormField {:required mandatory}
   [:label displayName nbsp (help-popup description)]
   [ui/Input
    {:type          "text"
     :name          param-name
     :default-value (or data "")
     :read-only     readOnly
     :fluid         true
     :on-change     (ui-utils/callback :value
                                       #(update-fn form-id param-name %))}]])


(defmethod form-field :hidden
  [_ _ [param-name {:keys [data]}]]
  ^{:key param-name}
  [:input {:name  param-name
           :type  "hidden"
           :value (or data "")}])


(defmethod form-field :password
  [update-fn form-id [param-name {:keys [data displayName readOnly mandatory description]}]]
  ^{:key displayName}
  [ui/FormField {:required mandatory}
   [:label displayName nbsp (help-popup description)]
   [ui/Input
    {:type          "password"
     :name          param-name
     :default-value (or data "")
     :read-only     readOnly
     :fluid         true
     :on-change     (ui-utils/callback :value
                                       #(update-fn form-id param-name %))}]])


(defmethod form-field :int
  [update-fn form-id [param-name {:keys [data displayName readOnly mandatory description]}]]
  ^{:key displayName}
  [ui/FormField {:required mandatory}
   [:label displayName nbsp (help-popup description)]
   [ui/Input
    {:type          "number"
     :name          param-name
     :default-value (or data "")
     :read-only     readOnly
     :fluid         true
     :on-change     (ui-utils/callback :value
                                       #(update-fn form-id param-name (utils/str->int %)))}]])


(defmethod form-field :boolean
  [update-fn form-id [param-name {:keys [data displayName readOnly mandatory description]}]]
  ^{:key displayName}
  [ui/FormField {:required mandatory}
   [:label displayName nbsp (help-popup description)]
   [ui/Checkbox
    {:default-value false
     :read-only     readOnly
     :on-change     (ui-utils/callback :checked
                                       #(update-fn form-id param-name %))}]])


(defmethod form-field :enum
  [update-fn form-id [param-name {:keys [enum displayName mandatory description]}]]
  ^{:key displayName}
  [ui/FormField {:required mandatory}
   [:label displayName nbsp (help-popup description)]
   [ui/FormSelect
    {:options   (vec (map (fn [v] {:value v, :text v}) enum))
     :on-change (ui-utils/callback :value
                                   #(update-fn form-id param-name %))}]])
