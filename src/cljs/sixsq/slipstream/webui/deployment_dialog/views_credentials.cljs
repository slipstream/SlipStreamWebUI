(ns sixsq.slipstream.webui.deployment-dialog.views-credentials
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn list-item
  [{:keys [id name description created] :as credential}]
  (let [selected-credential (subscribe [::subs/selected-credential])]
    ^{:key id}
    (let [{selected-id :id} @selected-credential]
      [ui/ListItem {:active   (= id selected-id)
                    :on-click #(dispatch [::events/set-selected-credential credential])}
       [ui/ListIcon {:name "key", :size "large", :vertical-align "middle"}]
       [ui/ListContent
        [ui/ListHeader (str (or name id) " (" (time/ago (time/parse-iso8601 created)) ")")]
        (or description "")]])))


(defn content
  []
  (dispatch [::events/get-credentials])
  (fn []
    (let [tr (subscribe [::i18n-subs/tr])
          loading? (subscribe [::subs/loading-credentials?])
          credentials (subscribe [::subs/credentials])]
      (if (seq @credentials)
        (vec (concat [ui/ListSA {:divided   true
                                 :relaxed   true
                                 :selection true}]
                     (mapv list-item @credentials)))
        [ui/Message {:error true} (@tr [:no-credentials])]))))
