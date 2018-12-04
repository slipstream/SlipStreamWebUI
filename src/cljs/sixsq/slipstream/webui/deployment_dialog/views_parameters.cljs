(ns sixsq.slipstream.webui.deployment-dialog.views-parameters
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.deployment-dialog.utils :as utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))


(defn summary-item
  []
  (let [tr (subscribe [::i18n-subs/tr])
        filtered-params (subscribe [::subs/filtered-input-parameters])

        header (@tr [:parameters])
        description (str "Number of parameters: " (count @filtered-params))
        on-click-fn #(dispatch [::events/set-active-step :parameters])]

    ^{:key "parameters"}
    [ui/ListItem {:active   false
                  :on-click on-click-fn}
     [ui/ListIcon {:name "list alternate outline", :size "large", :vertical-align "middle"}]
     [ui/ListContent
      [ui/ListHeader header]
      [ui/ListDescription description]]]))


(defn as-form-input
  [{:keys [parameter description value] :as param}]
  (let [deployment (subscribe [::subs/deployment])]
    ^{:key parameter}
    [ui/FormField
     [:label parameter ff/nbsp (ff/help-popup description)]
     [ui/Input
      {:type          "text"
       :name          parameter
       :default-value (or value "")
       :read-only     false
       :fluid         true
       :on-blur       (ui-callback/input-callback
                        (fn [new-value]
                          (let [updated-deployment (utils/update-parameter-in-deployment parameter new-value @deployment)]
                            (dispatch [::events/set-deployment updated-deployment]))))}]]))


(defn content
  []
  (let [tr (subscribe [::i18n-subs/tr])
        filtered-params (subscribe [::subs/filtered-input-parameters])]

    (if (seq @filtered-params)
      (vec (concat [ui/Form]
                   (map as-form-input @filtered-params)))
      [ui/Message {:success true} (@tr [:no-input-parameters])])))
