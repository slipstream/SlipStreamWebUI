(ns sixsq.slipstream.webui.deployment-dialog.views-data
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))



(defn list-item
  [{:keys [key doc_count]}]
  (let [selected-cloud (subscribe [::subs/selected-cloud])
        connectors (subscribe [::subs/connectors])
        {:keys [name description]} (get @connectors key)]
    ^{:key key}
    [ui/ListItem {:active   (= key @selected-cloud)
                  :on-click #(dispatch [::events/set-cloud-filter key])}
     [ui/ListIcon {:name "cloud", :size "large", :vertical-align "middle"}]
     [ui/ListContent
      [ui/ListHeader (or name key)]
      (when description
        [ui/ListDescription description])
      [:span (str "Number of data objects: " (or doc_count ""))]]]))


(defn content
  []
  (let [tr (subscribe [::i18n-subs/tr])
        data-clouds (subscribe [::subs/data-clouds])]
    (if (seq @data-clouds)
      (vec (concat [ui/ListSA {:divided   true
                               :relaxed   true
                               :selection true}]
                   (mapv list-item @data-clouds)))
      [ui/Message {:error true} (@tr [:no-data-location])])))
