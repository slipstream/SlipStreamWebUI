(ns sixsq.slipstream.webui.deployment-dialog.views-size
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))


(defn summary-item
  [_ {:keys [cpu ram disk] :as size}]
  (let [tr (subscribe [::i18n-subs/tr])
        header (@tr [:size])
        description (str "CPU: " cpu ",  RAM:" ram " MB,  DISK: " disk " GB")
        on-click-fn #(dispatch [::events/set-active-step :size])]

    ^{:key "size"}
    [ui/ListItem {:active   false
                  :on-click on-click-fn}
     [ui/ListIcon {:name "expand arrows alternate", :size "large", :vertical-align "middle"}]
     [ui/ListContent
      [ui/ListHeader header]
      [ui/ListDescription description]]]))


(defn input-size
  [name property-key]
  (let [deployment (subscribe [::subs/deployment])
        size (select-keys (get-in @deployment [:module :content])
                          #{:cpu :ram :disk})]
    ^{:key (str (:id @deployment) "-" name)}
    [ui/FormInput {:type          "number",
                   :label         name,
                   :default-value (get-in @deployment [:module :content property-key]),
                   :on-blur       (ui-callback/input-callback
                                    (fn [new-value]
                                      (let [new-int (int new-value)
                                            new-size (assoc size property-key new-int)]
                                        (dispatch
                                          [::events/set-deployment
                                           (assoc-in @deployment [:module :content property-key] new-int)])
                                        (dispatch [::events/set-size-summary (summary-item {} new-size)]))))}]))


(defn content
  []
  [ui/Form
   [input-size "CPU" :cpu]
   [input-size "RAM [MB]" :ram]
   [input-size "DISK [GB]" :disk]])
