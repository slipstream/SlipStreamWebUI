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


(defn remove-input-params
  [collection set-params-to-remove]
  (remove #(set-params-to-remove (:parameter %)) collection))


(defn content
  []
  (let [tr (subscribe [::i18n-subs/tr])
        deployment (subscribe [::subs/deployment])
        selected-credential (subscribe [::subs/selected-credential])]
    (let [is-not-docker? (not= (:type @selected-credential) "cloud-cred-docker")
          params-to-filter (cond-> #{"credential.id"}
                                   is-not-docker? (conj "cloud.node.publish.ports"))
          params (-> @deployment
                     :module
                     :content
                     :inputParameters
                     (remove-input-params params-to-filter))]
      (if (seq params)
        (vec (concat [ui/Form]
                     (map as-form-input params)))
        [ui/Message {:success true} (@tr [:no-input-parameters])]))))
