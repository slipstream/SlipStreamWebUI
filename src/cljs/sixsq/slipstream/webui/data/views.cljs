(ns sixsq.slipstream.webui.data.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.application.utils :as application-utils]
    [sixsq.slipstream.webui.data.events :as events]
    [sixsq.slipstream.webui.data.subs :as subs]
    [sixsq.slipstream.webui.deployment-dialog.events :as deployment-dialog-events]
    [sixsq.slipstream.webui.deployment-dialog.views :as deployment-dialog-views]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn refresh []
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
        locale (subscribe [::i18n-subs/locale])]
    (fn []
      (let [[time-start time-end] @time-period]
        [ui/Form {:widths "equal"}
         [ui/FormGroup
          [ui/FormField
           ;; FIXME: Find a better way to set the field width.
           [ui/DatePicker {:custom-input     (reagent/as-element [ui/Input {:label (@tr [:from])
                                                                            :style {:min-width "25em"}}])
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
                           :on-change        #(dispatch [::events/set-time-period [% time-end]])}]]
          ;; FIXME: Find a better way to set the field width.
          [ui/FormField
           [ui/DatePicker {:custom-input     (reagent/as-element [ui/Input {:label (@tr [:to])
                                                                            :style {:min-width "25em"}}])
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
                           :on-change        #(dispatch [::events/set-time-period [time-start %]])}]]]]))))


(defn control-bar []
  [:div
   [ui/Menu {:attached "top", :borderless true}
    [refresh-button]]
   [ui/Segment {:attached "bottom"}
    [search-header]]])


(defn application-list-item
  [{:keys [id name description type created] :as application}]
  ^{:key id}
  [ui/ListItem {:on-click #(do
                             (dispatch [::events/close-application-select-modal])
                             (dispatch [::deployment-dialog-events/create-deployment id "data"]))}
   [ui/ListIcon {:name (application-utils/category-icon type), :size "large", :vertical-align "middle"}]
   [ui/ListContent
    [ui/ListHeader (str (or name id) " (" (time/ago (time/parse-iso8601 created)) ")")]
    (or description "")]])


(defn application-list
  []
  (let [tr (subscribe [::i18n-subs/tr])
        applications (subscribe [::subs/applications])
        loading? (subscribe [::subs/loading-applications?])]
    [ui/Segment {:loading @loading?
                 :basic   true}
     (if (seq @applications)
       (vec (concat [ui/ListSA {:divided   true
                                :relaxed   true
                                :selection true}]
                    (mapv application-list-item @applications)))
       [ui/Message {:error true} (@tr [:no-apps])])]))


(defn application-select-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        visible? (subscribe [::subs/application-select-visible?])]
    (fn []
      (let [hide-fn #(dispatch [::events/close-application-select-modal])]
        [ui/Modal {:open       @visible?
                   :close-icon true
                   :on-close   hide-fn}

         [ui/ModalHeader [ui/Icon {:name "sitemap"}] "\u00a0" (@tr [:select-application])]

         [ui/ModalContent {:scrolling true}
          [ui/ModalDescription
           [application-list]]]]))))


(defn format-data-query
  [{:keys [id name description] :as data-query}]
  (let [tr (subscribe [::i18n-subs/tr])
        data (subscribe [::subs/data])
        count (get @data id "...")]
    ^{:key id}
    [ui/Card
     [ui/CardContent
      [ui/CardHeader {:style {:word-wrap "break-word"}} (or name id)]
      [ui/CardMeta description]
      [ui/CardDescription {:style {:word-wrap "break-word"}} (@tr [:count]) ": " count]]
     [ui/Button {:fluid    true
                 :primary  true
                 :on-click #(dispatch [::events/open-application-select-modal id])}
      (@tr [:process])]]))


(defn queries-cards-group
  []
  (let [data-queries (subscribe [::subs/data-queries])]
    [ui/Segment style/basic
     (vec (concat [ui/CardGroup]
                  (map (fn [data-query]
                         [format-data-query data-query])
                       (vals @data-queries))))]))


(defn service-offer-resources
  []
  [ui/Container {:fluid true}
   [control-bar]
   [application-select-modal]
   [deployment-dialog-views/deploy-modal true]
   [queries-cards-group]])


(defmethod panel/render :data
  [path]
  (refresh)
  [service-offer-resources])
