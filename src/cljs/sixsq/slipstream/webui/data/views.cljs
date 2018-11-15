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
    [sixsq.slipstream.webui.application.utils :as application-utils]
    [sixsq.slipstream.webui.appstore.views :as appstore-views]
    [sixsq.slipstream.webui.appstore.events :as appstore-events]
    [taoensso.timbre :as log]
    [reagent.core :as reagent]))


(defn refresh []
  ;(dispatch [::events/get-service-offers])  ;; unused, all information taken with get-content-types
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


(defn application-list-item
  [{:keys [id name description type created] :as application}]
  ^{:key id}
  [ui/ListItem {:on-click #(do
                             (dispatch [::events/close-application-select-modal])
                             (dispatch [::appstore-events/create-deployment id]))}
   [ui/ListIcon {:name (application-utils/category-icon type), :size "large", :vertical-align "middle"}]
   [ui/ListContent
    [ui/ListHeader (str (or name id) " (" (time/ago (time/parse-iso8601 created)) ")")]
    (or description "")]])


(defn application-list
  []
  (let [applications (subscribe [::subs/applications])]
    (vec (concat [ui/ListSA {:divided   true
                             :relaxed   true
                             :selection true}]
                 (mapv application-list-item @applications)))))

(defn application-select-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        visible? (subscribe [::subs/application-select-visible?])
        loading? (subscribe [::subs/loading-applications?])]
    (fn []
      (let [hide-fn #(dispatch [::events/close-application-select-modal])
            submit-fn #()                                   ;#(dispatch [::events/edit-deployment])
            ]
        [ui/Modal {:open       @visible?
                   :close-icon true
                   :on-close   hide-fn}

         [ui/ModalHeader [ui/Icon {:name "play"}] (@tr [:deploy]) " \u2014 "
          ;(get-in @deployment [:module :path])
          ]

         [ui/ModalContent {:scrolling true}
          [ui/Segment {:attached true, :loading @loading?}
           [application-list]

           ;;[ui/StepGroup {:attached "top"}
           ;; [deployment-step "summary" "info" "An overview of the application to be deployed."]
           ;; #_[deployment-step "offers" "check" "Resource constraints and service offers."]
           ;; [deployment-step "size" "resize vertical" "Infrastructure cpu ram disk to use for the deployment."]
           ;; [deployment-step "credentials" "key" "Infrastructure credentials to use for the deployment."]
           ;; [deployment-step "parameters" "list alternate outline" "Input parameters for the application."]]
           ;;(case @step-id
           ;;  "summary" [deployment-summary]
           ;;  "offers" [deployment-resources]
           ;;  "parameters" [deployment-params]
           ;;  "credentials" [credential-list]
           ;;  "size" [deployment-resources]
           ;;  nil)
           ]]

         [ui/ModalActions
          ;;[uix/Button {:text     (@tr [:cancel]),
          ;;             :on-click hide-fn
          ;;             :disabled (not (:id @deployment))}]
          ;;[uix/Button {:text     (@tr [:deploy]), :primary true,
          ;;             :on-click #(submit-fn)
          ;;             }]
          ]]))))


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
                 :on-click #(dispatch [::events/open-application-select-modal key])}
      (@tr [:process])]]))


(defn content-types-cards-group
  []
  (let [content-types (subscribe [::subs/content-types])]
    [ui/Segment style/basic
     (vec (concat [ui/CardGroup]
                  (map (fn [content-type]
                         [format-content-type content-type])
                       @content-types)))]))


(defn service-offer-resources
  []
  [ui/Container {:fluid true}
   [control-bar]
   [application-select-modal]
   [appstore-views/deploy-modal]
   [content-types-cards-group]
   ])

(defmethod panel/render :data
  [path]
  (refresh)
  [service-offer-resources])
