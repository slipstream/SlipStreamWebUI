(ns sixsq.slipstream.webui.deployment-dialog.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.views-credentials :as credentials-step]
    [sixsq.slipstream.webui.deployment-dialog.views-data :as data-step]
    [sixsq.slipstream.webui.deployment-dialog.views-parameters :as parameters-step]
    [sixsq.slipstream.webui.deployment-dialog.views-size :as size-step]
    [sixsq.slipstream.webui.deployment-dialog.views-summary :as summary-step]
    [sixsq.slipstream.webui.deployment-dialog.events :as events]
    [sixsq.slipstream.webui.deployment-dialog.spec :as spec]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]))


(defn deployment-step-state
  [{:keys [step-id completed? icon] :as step-state}]
  (let [tr (subscribe [::i18n-subs/tr])
        active-step (subscribe [::subs/active-step])]
    [ui/Step {:link      true
              :on-click  #(dispatch [::events/set-active-step step-id])
              :completed completed?
              :active    (= step-id @active-step)}
     [ui/Icon {:name icon}]
     [ui/StepContent
      [ui/StepTitle (@tr [step-id])]]]))


(defn step-content
  [active-step]
  (case active-step
    :data [data-step/content]
    :credentials [credentials-step/content]
    :size [size-step/content]
    :parameters [parameters-step/content]
    :summary [summary-step/content]
    nil))


(defn deploy-modal
  [show-data?]
  (let [tr (subscribe [::i18n-subs/tr])
        visible? (subscribe [::subs/deploy-modal-visible?])
        deployment (subscribe [::subs/deployment])
        active-step (subscribe [::subs/active-step])
        step-states (subscribe [::subs/step-states])]
    (fn [show-data?]
      (let [hide-fn #(dispatch [::events/close-deploy-modal])
            submit-fn #(dispatch [::events/edit-deployment])
            visible-steps (if show-data? spec/steps (rest spec/steps))]

        [ui/Modal {:open       @visible?
                   :close-icon true
                   :on-close   hide-fn}

         [ui/ModalHeader
          [ui/Icon {:name "rocket", :size "large"}]
          "\u00a0"
          (get-in @deployment [:module :name])]

         [ui/ModalContent {:scrolling true}
          [ui/ModalDescription {:style {:height "30em"}}

           (vec (concat [ui/StepGroup {:size "mini", :fluid true}]
                        (->> visible-steps
                             (map #(get @step-states %))
                             (mapv deployment-step-state))))

           [ui/Segment {:basic true, :style {:padding 0}}
            [step-content @active-step]]]]

         [ui/ModalActions
          [uix/Button {:text     (@tr [:cancel])
                       :on-click hide-fn
                       :disabled (not (:id @deployment))}]
          [uix/Button {:text     (@tr [:launch])
                       :primary  true
                       :disabled (not= @active-step :summary)
                       :on-click submit-fn}]]]))))
