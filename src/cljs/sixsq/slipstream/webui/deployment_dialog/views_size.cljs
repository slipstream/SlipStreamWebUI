(ns sixsq.slipstream.webui.deployment-dialog.views-size
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))


(defn summary-item
  []
  (let [tr (subscribe [::i18n-subs/tr])
        size (subscribe [::subs/size])

        {:keys [cpu ram disk]} @size

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
        size (subscribe [::subs/size])]
    ^{:key (str (:id @deployment) "-" name)}
    [ui/FormInput {:type          "number",
                   :label         name,
                   :default-value (get @size property-key),
                   :on-blur       (ui-callback/input-callback
                                    (fn [new-value]
                                      (let [new-int (int new-value)]
                                        (dispatch
                                          [::events/set-deployment
                                           (assoc-in @deployment [:module :content property-key] new-int)]))))}]))


(defn content
  []
  [ui/Form
   [input-size "CPU" :cpu]
   [input-size "RAM [MB]" :ram]
   [input-size "DISK [GB]" :disk]])
