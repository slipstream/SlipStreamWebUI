(ns sixsq.slipstream.webui.deployment-dialog.views-credentials
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn cred-list-item
  [{:keys [id header description active on-click-fn] :as credential}]
  ^{:key id}
  [ui/ListItem {:active   active
                :on-click on-click-fn}
   [ui/ListIcon {:name "key", :size "large", :vertical-align "middle"}]
   [ui/ListContent
    [ui/ListHeader header]
    (when description
      [ui/ListDescription description])]])


(defn item-options
  [{:keys [id name description] :as credential}]
  {:id          id
   :header      (or name id)
   :description description})


(defn summary-item
  []
  (let [selected-credential (subscribe [::subs/selected-credential])

        options (merge (item-options @selected-credential)
                       {:key         :credentials
                        :active      false
                        :on-click-fn #(dispatch [::events/set-active-step :credentials])})]
    [cred-list-item options]))


(defn list-item
  [{:keys [id] :as credential}]
  (let [selected-credential (subscribe [::subs/selected-credential])]
    ^{:key id}
    (let [{selected-id :id} @selected-credential

          options (assoc (item-options credential) :active (= id selected-id))
          on-click-fn #(dispatch [::events/set-selected-credential credential])]

      [cred-list-item (assoc options :on-click-fn on-click-fn)])))


(defn content
  []
  (fn []
    (let [tr (subscribe [::i18n-subs/tr])
          credentials (subscribe [::subs/credentials])]

      (if (seq @credentials)
        (vec (concat [ui/ListSA {:divided   true
                                 :relaxed   true
                                 :selection true}]
                     (mapv list-item @credentials)))
        [ui/Message {:error true} (@tr [:no-credentials])]))))
