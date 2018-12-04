(ns sixsq.slipstream.webui.deployment-dialog.views-size
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))


(defn input-size
  [name property-key]
  (let [deployment (subscribe [::subs/deployment])]
    ^{:key (str (:id @deployment) "-" name)}
    [ui/FormInput {:type          "number",
                   :label         name,
                   :default-value (get-in @deployment [:module :content property-key]),
                   :on-blur       (ui-callback/input-callback
                                    (fn [new-value]
                                      (dispatch
                                        [::events/set-deployment
                                         (assoc-in @deployment [:module :content property-key] (int new-value))])))}]))


(defn content
  []
  [ui/Form
   [input-size "CPU" :cpu]
   [input-size "RAM [MB]" :ram]
   [input-size "DISK [GB]" :disk]])
