(ns sixsq.slipstream.webui.activity.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button md-circle-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title popover-anchor-wrapper popover-content-wrapper popover-tooltip]
     :refer-macros [handler-fn]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.activity.effects]
    [sixsq.slipstream.webui.activity.events]
    [sixsq.slipstream.webui.activity.subs]
    [clojure.string :as str]))

(defn runs-control []
  (let [tr (subscribe [:i18n-tr])
        offset (reagent/atom "1")
        limit (reagent/atom "10")
        cloud (reagent/atom "")
        activeOnly (reagent/atom true)]
    (fn []
      [h-box
       :gap "3px"
       :children [[input-text
                   :model offset
                   :placeholder (@tr [:offset])
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! offset v)
                                (dispatch [:set-runs-params {:offset v}]))]
                  [input-text
                   :model limit
                   :placeholder (@tr [:limit])
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! limit v)
                                (dispatch [:set-runs-params {:limit v}]))]
                  [input-text
                   :model cloud
                   :placeholder (@tr [:cloud])
                   :width "300px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! cloud v)
                                (dispatch [:set-runs-params {:cloud v}]))]
                  [checkbox
                   :model activeOnly
                   :label (@tr [:active-only])
                   :on-change (fn [v]
                                (reset! activeOnly v)
                                (dispatch [:set-runs-params {:activeOnly (if v 1 0)}]))]
                  [button
                   :label (@tr [:show])
                   :on-click #(dispatch [:runs-search])]
                  ]])))

(defn service-url
  [url status]
  [h-box
   :width "2ex"
   :justify :center
   :children [(if (and (= status "Ready") (not (str/blank? url)))
                [hyperlink-href
                 :label [:i {:class (str "zmdi zmdi-hc-fw-rc zmdi-mail-reply")}]
                 :href url
                 :target "_blank"])]])

(def curr-position (reagent/atom :below-center))
(def positions [{:id :above-left :label ":above-left  "}
                {:id :above-center :label ":above-center"}
                {:id :above-right :label ":above-right "}
                {:id :below-left :label ":below-left  "}
                {:id :below-center :label ":below-center"}
                {:id :below-right :label ":below-right "}
                {:id :left-above :label ":left-above  "}
                {:id :left-center :label ":left-center "}
                {:id :left-below :label ":left-below  "}
                {:id :right-above :label ":right-above "}
                {:id :right-center :label ":right-center"}
                {:id :right-below :label ":right-below "}])

(defn format-module
  [module]
  (let [showing? (reagent/atom false)
        tag (second (reverse (str/split module #"/")))]
    (fn []
      (let [module-label [label
                          :width "20ex"
                          :label tag
                          :attr {:on-mouse-over (handler-fn (reset! showing? true))
                                 :on-mouse-out  (handler-fn (reset! showing? false))}]]
        [popover-tooltip
         :label module
         :showing? showing?
         :anchor module-label]))))

(defn format-uuid
  [uuid]
  (let [showing? (reagent/atom false)
        tag (.substring uuid 0 8)]
    (fn []
      (let [uuid-label [label
                        :width "10ex"
                        :label tag
                        :attr {:on-mouse-over (handler-fn (reset! showing? true))
                               :on-mouse-out  (handler-fn (reset! showing? false))}]]
        [popover-tooltip
         :label uuid
         :showing? showing?
         :anchor uuid-label]))))

(defn abort-popup [message]
  (fn []
    [h-box
     :width "2ex"
     :justify :center
     :children [(when-not (str/blank? message)
                  [row-button
                   :md-icon-name "zmdi zmdi-notifications-active"
                   :mouse-over-row? true
                   :tooltip message
                   :on-click #()])]]))

(defn deployment-icon [type]
  (let [icon (case type
               "Orchestration" "zmdi-apps"
               "Run" "zmdi-widgets"
               "zmdi-minus")]
    [row-button
     :md-icon-name (str "zmdi " icon)
     :mouse-over-row? true
     :on-click #()]))

(defn format-run
  [{:keys [cloudServiceNames
           tags
           resourceUri
           startTime
           username
           type
           activeVm
           abort
           moduleResourceUri
           status
           uuid
           serviceUrl] :as run}]
  [h-box
   :gap "2px"
   :children [[label :width "2ex" :label [deployment-icon type]]
              [abort-popup abort]
              (service-url serviceUrl status)
              [format-uuid uuid]
              [format-module moduleResourceUri]
              [label :width "5ex" :label activeVm]
              [label :width "10ex" :label status]
              [label :width "15ex" :label username]
              [label :width "30ex" :label startTime]
              [label :width "20ex" :label cloudServiceNames]
              [label :width "40px" :label tags]]])

(defn run-header []
  (let [tr (subscribe [:i18n-tr])]
    (fn []
      [h-box
       :gap "2px"
       :children [[label :width "2ex" :label ""]
                  [abort-popup ""]
                  (service-url "" "")
                  [label :width "10ex" :label (@tr [:id])]
                  [label :width "20ex" :label (@tr [:module])]
                  [label :width "5ex" :label (@tr [:vms])]
                  [label :width "10ex" :label (@tr [:status])]
                  [label :width "15ex" :label (@tr [:username])]
                  [label :width "30ex" :label (@tr [:start])]
                  [label :width "20ex" :label (@tr [:cloud])]
                  [label :width "40px" :label (@tr [:tag])]]])))

(defn runs-display
  []
  (let [runs-data (subscribe [:runs-data])]
    (fn []
      (if-let [{:keys [runs]} @runs-data]
        (let [{:keys [count totalCount item]} runs]
          [v-box
           :gap "3px"
           :children [[label
                       :label (str count "/" totalCount)]
                      [v-box
                       :children (concat [[run-header]] (vec (map format-run item)))]]])))))

(defn runs-panel
  []
  (fn []
    [v-box
     :gap "3px"
     :children [[runs-control]
                [runs-display]]]))

