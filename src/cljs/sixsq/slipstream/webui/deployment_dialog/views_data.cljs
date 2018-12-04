(ns sixsq.slipstream.webui.deployment-dialog.views-data
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn cloud-list-item
  [{:keys [key active on-click-fn header description doc_count]}]
  (let [tr (subscribe [::i18n-subs/tr])]
    ^{:key key}
    [ui/ListItem (cond-> {:active active}
                         on-click-fn (assoc :on-click on-click-fn))
     [ui/ListIcon {:name "cloud", :size "large", :vertical-align "middle"}]
     [ui/ListContent
      [ui/ListHeader header]
      (when description
        [ui/ListDescription description])
      [:span (@tr [:object-count] [(or doc_count "...")])]]]))


(defn summary-list-item
  [{:keys [header description doc_count]}]
  [cloud-list-item {:key         :data
                    :active      false
                    :on-click-fn #(dispatch [::events/set-active-step :data])
                    :header      header
                    :description description
                    :doc_count   doc_count}])


(defn list-item
  [{:keys [key doc_count]}]
  (let [selected-cloud (subscribe [::subs/selected-cloud])
        connectors (subscribe [::subs/connectors])
        {:keys [name description]} (get @connectors key)]

    (let [options {:key         key
                   :active      (= key @selected-cloud)
                   :header      (or name key)
                   :description description
                   :doc_count   doc_count}
          summary-item [summary-list-item options]
          on-click-fn #(dispatch [::events/set-cloud-filter key summary-item])]

      [cloud-list-item (assoc options :on-click-fn on-click-fn)])))


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
