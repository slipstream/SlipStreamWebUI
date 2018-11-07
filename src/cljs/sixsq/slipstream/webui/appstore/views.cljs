(ns sixsq.slipstream.webui.appstore.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.appstore.events :as appstore-events]
    [sixsq.slipstream.webui.appstore.subs :as appstore-subs]
    [sixsq.slipstream.webui.appstore.utils :as utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.time :as time]
    [sixsq.slipstream.webui.deployment-detail.utils :as deployment-detail-utils]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]))


(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn []
      [uix/MenuItemWithIcon
       {:name      (@tr [:refresh])
        :icon-name "refresh"
        :on-click  #(dispatch [::appstore-events/get-deployment-templates])}])))

(defn control-bar []
  (let [tr (subscribe [::i18n-subs/tr])]
    [:div
     [ui/Menu {:attached "top", :borderless true}
      [refresh-button]
      [ui/MenuMenu {:position "right"}
       [ui/MenuItem
        [ui/Input {:placeholder (@tr [:search])
                   :icon        "search"
                   :on-change   (ui-callback/input-callback #(dispatch [::appstore-events/set-full-text-search %]))}]]
       ]]]))


(defn format-deployment-template
  [{:keys [id name description module] :as deployment-template}]
  (let [tr (subscribe [::i18n-subs/tr])
        {:keys [type parentPath logo]} module]
    ^{:key id}
    [ui/Card
     (when logo
       [ui/Image {:src   (:href logo)
                  :style {:width      "auto"
                          :height     "100px"
                          :object-fit "contain"}}])
     [ui/CardContent
      [ui/CardHeader {:style {:word-wrap "break-word"}}
       [ui/Icon {:name (deployment-detail-utils/category-icon type)}]
       (or name id)]
      [ui/CardMeta {:style {:word-wrap "break-word"}} parentPath]
      [ui/CardDescription {:style {:overflow "hidden" :max-height "100px"}} description]]
     [ui/Button {:fluid    true
                 :primary  true
                 :on-click #(dispatch [::appstore-events/create-deployment id])}
      (@tr [:deploy])]]))


(defn deployment-templates-cards-group
  [deployment-templates-list]
  [ui/Segment style/basic
   (vec (concat [ui/CardGroup]
                (map (fn [deployment-template]
                       [format-deployment-template deployment-template])
                     deployment-templates-list)))])


(defn deployment-summary
  []
  (let [deployment (subscribe [::appstore-subs/deployment])]
    (let [{:keys [id name description module]} @deployment]
      [ui/ListSA
       [ui/ListItem id]
       [ui/ListItem name]
       [ui/ListItem description]
       [ui/ListItem (:path module)]
       ])))


(defn input-size
  [name property-key]
  (let [deployment (subscribe [::appstore-subs/deployment])]
    ^{:key (str (:id @deployment) "-" name)}
    [ui/FormInput {:type          "number",
                   :label         name,
                   :default-value (get-in @deployment [:module :content property-key]),
                   :on-blur       (ui-callback/input-callback
                                    (fn [new-value]
                                      (dispatch
                                        [::appstore-events/set-deployment
                                         (assoc-in @deployment [:module :content property-key] (int new-value))])))}]))

(defn deployment-resources
  []
  [ui/Form
   [input-size "CPU" :cpu]
   [input-size "RAM [MB]" :ram]
   [input-size "DISK [GB]" :disk]])


(defn as-form-input
  [{:keys [parameter description value] :as param}]
  (let [deployment (subscribe [::appstore-subs/deployment])]
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
                            (dispatch [::appstore-events/set-deployment updated-deployment]))))}]]))


(defn deployment-params
  []
  (let [template (subscribe [::appstore-subs/deployment])]
    (let [params (-> @template :module :content :inputParameters)]
      (vec (concat [ui/Form]
                   (map as-form-input params))))))


(defn credential-list-item
  [{:keys [id name description created] :as credential}]
  (let [selected-credential (subscribe [::appstore-subs/selected-credential])]
    ^{:key id}
    (let [{selected-id :id} @selected-credential
          icon-name (if (= id selected-id) "check circle outline" "circle outline")]
      [ui/ListItem {:on-click #(dispatch [::appstore-events/set-selected-credential credential])}
       [ui/ListIcon {:name icon-name, :size "large", :vertical-align "middle"}]
       [ui/ListContent
        [ui/ListHeader (str (or name id) " (" (time/ago (time/parse-iso8601 created)) ")")]
        (or description "")]])))


(defn credential-list
  []
  (let [credentials (subscribe [::appstore-subs/credentials])]
    (vec (concat [ui/ListSA {:divided   true
                             :relaxed   true
                             :selection true}]
                 (mapv credential-list-item @credentials)))))

(defn deployment-step
  [name icon description]
  (let [step-id (subscribe [::appstore-subs/step-id])]
    [ui/Step {:icon     icon
              :title    name
              ;:description description
              :on-click #(dispatch [::appstore-events/set-step-id name])
              :active   (= name @step-id)}]))

(defn deploy-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        visible? (subscribe [::appstore-subs/deploy-modal-visible?])
        deployment (subscribe [::appstore-subs/deployment])
        loading? (subscribe [::appstore-subs/loading-deployment?])
        step-id (subscribe [::appstore-subs/step-id])]
    (fn []
      (let [hide-fn #(dispatch [::appstore-events/close-deploy-modal])
            submit-fn #(dispatch [::appstore-events/edit-deployment])]
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

(defn deployment-template-resources
  []
  (let [deployment-templates (subscribe [::appstore-subs/deployment-templates])
        elements-per-page (subscribe [::appstore-subs/elements-per-page])
        page (subscribe [::appstore-subs/page])]
    (fn []
      (let [total-pages (general-utils/total-pages (get @deployment-templates :count 0) @elements-per-page)]
        [ui/Container {:fluid true}
         [control-bar]
         [deploy-modal]
         [deployment-templates-cards-group (get @deployment-templates :deploymentTemplates [])]
         (when (> total-pages 1)
           [uix/Pagination
            {:totalPages   total-pages
             :activePage   @page
             :onPageChange (ui-callback/callback :activePage #(dispatch [::appstore-events/set-page %]))}])]))))


(defmethod panel/render :appstore
  [path]
  (dispatch [::appstore-events/get-deployment-templates])
  [deployment-template-resources])
