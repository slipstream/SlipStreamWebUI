(ns sixsq.slipstream.webui.data.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.data.events :as events]
    [sixsq.slipstream.webui.data.subs :as subs]
    [sixsq.slipstream.webui.data.utils :as utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.time :as time]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]
    [reagent.core :as reagent]))


(defn refresh []
  (dispatch [::events/get-service-offers])
  (dispatch [::events/get-content-types])
  (dispatch [::events/get-credentials]))

(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn []
      [uix/MenuItemWithIcon
       {:name      (@tr [:refresh])
        :icon-name "refresh"
        :on-click  refresh}])))

(defn search-header []
  (let [tr (subscribe [::i18n-subs/tr])
        time-period (subscribe [::subs/time-period])
        locale (subscribe [::i18n-subs/locale])
        ;billable-only? (subscribe [::usage-subs/billable-only?])
        ;range-initial-val u/default-date-range
        ;range-dropdown (reagent/atom range-initial-val)
        ]
    (fn []
      (let [[time-start time-end] @time-period
            ]
        [ui/Form
         [ui/FormGroup
          [ui/FormField
           [ui/DatePicker {:custom-input     (reagent/as-element [ui/Input {:label (@tr [:from])}])
                           :selected         time-start
                           :start-date       time-start
                           :end-date         time-end
                           :max-date         time-end
                           :selects-start    true
                           :show-time-select true
                           :time-format      "HH:mm"
                           :time-intervals   15
                           :locale           @locale
                           :fixed-height     true
                           :date-format      "LLL"
                           :on-change        #(dispatch [::events/set-time-period [% time-end]])
                           }]]
          [ui/FormField
           [ui/DatePicker {:custom-input     (reagent/as-element [ui/Input {:label (@tr [:to])}])
                           :selected         time-end
                           :start-date       time-start
                           :end-date         time-end
                           :min-date         time-start
                           :max-date         (time/now)
                           :selects-end      true
                           :show-time-select true
                           :time-format      "HH:mm"
                           :time-intervals   15
                           :locale           @locale
                           :fixed-height     true
                           :date-format      "LLL"
                           :on-change        #(dispatch [::events/set-time-period [time-start %]])
                           }]]]
         ]))))

(defn control-bar []
  (let [tr (subscribe [::i18n-subs/tr])]
    [:div
     [ui/Menu {:attached "top", :borderless true}
      [refresh-button]
      #_[ui/MenuMenu {:position "right"}
         [ui/MenuItem
          [ui/Input {:placeholder (@tr [:search])
                     :icon        "search"
                     :on-change   (ui-callback/input-callback #(dispatch [::events/set-full-text-search %]))}]]
         ]]
     [ui/Segment {:attached "bottom"}
      [search-header]]]))


(defn format-content-type
  [{:keys [key doc_count] :as content-type}]
  (let [tr (subscribe [::i18n-subs/tr])]
    ^{:key key}
    [ui/Card
     [ui/CardContent
      [ui/CardHeader {:style {:word-wrap "break-word"}} key]
      [ui/CardMeta {:style {:word-wrap "break-word"}} (@tr [:count]) ": " doc_count]
      #_[ui/CardDescription {:style {:overflow "hidden" :max-height "100px"}} description]]
     [ui/Button {:fluid    true
                 :primary  true
                 :on-click #()}
      (@tr [:process])]]))


(defn content-types-cards-group
  []
  (let [content-types (subscribe [::subs/content-types])]
    [ui/Segment style/basic
     (vec (concat [ui/CardGroup]
                  (map (fn [content-type]
                         [format-content-type content-type])
                       @content-types)))]))


(defn deployment-summary
  []
  (let [deployment (subscribe [::subs/deployment])]
    (let [{:keys [id name description module]} @deployment]
      [ui/ListSA
       [ui/ListItem id]
       [ui/ListItem name]
       [ui/ListItem description]
       [ui/ListItem (:path module)]
       ])))


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

(defn deployment-resources
  []
  [ui/Form
   [input-size "CPU" :cpu]
   [input-size "RAM [MB]" :ram]
   [input-size "DISK [GB]" :disk]])


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


(defn deployment-params
  []
  (let [template (subscribe [::subs/deployment])]
    (let [params (-> @template :module :content :inputParameters)]
      (vec (concat [ui/Form]
                   (map as-form-input params))))))


(defn credential-list-item
  [{:keys [id name description created] :as credential}]
  (let [selected-credential (subscribe [::subs/selected-credential])]
    ^{:key id}
    (let [{selected-id :id} @selected-credential
          icon-name (if (= id selected-id) "check circle outline" "circle outline")]
      [ui/ListItem {:on-click #(dispatch [::events/set-selected-credential credential])}
       [ui/ListIcon {:name icon-name, :size "large", :vertical-align "middle"}]
       [ui/ListContent
        [ui/ListHeader (str (or name id) " (" (time/ago (time/parse-iso8601 created)) ")")]
        (or description "")]])))


(defn credential-list
  []
  (let [credentials (subscribe [::subs/credentials])]
    (vec (concat [ui/ListSA {:divided   true
                             :relaxed   true
                             :selection true}]
                 (mapv credential-list-item @credentials)))))

(defn deployment-step
  [name icon description]
  (let [step-id (subscribe [::subs/step-id])]
    [ui/Step {:icon     icon
              :title    name
              ;:description description
              :on-click #(dispatch [::events/set-step-id name])
              :active   (= name @step-id)}]))

(defn deploy-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        visible? (subscribe [::subs/deploy-modal-visible?])
        deployment (subscribe [::subs/deployment])
        loading? (subscribe [::subs/loading-deployment?])
        step-id (subscribe [::subs/step-id])]
    (fn []
      (let [hide-fn #(dispatch [::events/close-deploy-modal])
            submit-fn #(dispatch [::events/edit-deployment])]
        [ui/Modal {:open       @visible?
                   :close-icon true
                   :on-close   hide-fn}

         [ui/ModalHeader [ui/Icon {:name "play"}] (@tr [:deploy]) " \u2014 " (get-in @deployment [:module :path])]

         [ui/ModalContent {:scrolling true}
          [ui/Segment {:attached true, :loading @loading?}
           [ui/StepGroup {:attached "top"}
            [deployment-step "summary" "info" "An overview of the application to be deployed."]
            #_[deployment-step "offers" "check" "Resource constraints and service offers."]
            [deployment-step "size" "resize vertical" "Infrastructure cpu ram disk to use for the deployment."]
            [deployment-step "credentials" "key" "Infrastructure credentials to use for the deployment."]
            [deployment-step "parameters" "list alternate outline" "Input parameters for the application."]]
           (case @step-id
             "summary" [deployment-summary]
             "offers" [deployment-resources]
             "parameters" [deployment-params]
             "credentials" [credential-list]
             "size" [deployment-resources]
             nil)]]

         [ui/ModalActions
          [uix/Button {:text     (@tr [:cancel]),
                       :on-click hide-fn
                       :disabled (not (:id @deployment))}]
          [uix/Button {:text     (@tr [:deploy]), :primary true,
                       :on-click #(submit-fn)
                       }]]]))))

(defn service-offer-resources
  []
  (let [service-offers (subscribe [::subs/service-offers])
        credentials (subscribe [::subs/credentials])
        deployment-templates (subscribe [::subs/deployment-templates])
        elements-per-page (subscribe [::subs/elements-per-page])
        page (subscribe [::subs/page])]
    (fn []
      (let [total-pages (general-utils/total-pages (get @deployment-templates :count 0) @elements-per-page)]
        [ui/Container {:fluid true}
         [control-bar]
         [content-types-cards-group]
         #_[deploy-modal]
         #_[deployment-templates-cards-group (get @deployment-templates :deploymentTemplates [])]
         #_(when (> total-pages 1)
             [uix/Pagination
              {:totalPages   total-pages
               :activePage   @page
               :onPageChange (ui-callback/callback :activePage #(dispatch [::events/set-page %]))}])]))))

(defmethod panel/render :data
  [path]
  (refresh)
  [service-offer-resources])
