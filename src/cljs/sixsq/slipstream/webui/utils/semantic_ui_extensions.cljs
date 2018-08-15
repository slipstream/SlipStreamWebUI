(ns sixsq.slipstream.webui.utils.semantic-ui-extensions
  (:require
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn Button
  "This button requires a single options map that contains the :text key. The
   value of the :text key is used to define the button text as well as the
   accessibility label :aria-label.  The button may not specify children."
  [{:keys [text] :as options}]
  (let [final-opts (-> options
                       (dissoc :text)
                       (assoc :aria-label text))]
    [ui/Button final-opts text]))


(defn MenuItemWithIcon
  "Provides a menu item that reuses the name for the :name property and as the
   MenuItem label. The optional icon-name specifies the icon to use. The
   loading? parameter specifies if the icon should be spinning."
  [{:keys [name icon-name loading?] :as options}]
  (let [final-opts (-> options
                       (dissoc :icon-name :loading?)
                       (assoc :aria-label name))]
    [ui/MenuItem final-opts
     (when icon-name
       [ui/Icon (cond-> {:name icon-name}
                        (boolean? loading?) (assoc :loading loading?))])
     name]))
