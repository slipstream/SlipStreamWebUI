(ns sixsq.slipstream.webui.activity.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title popover-anchor-wrapper popover-content-wrapper]
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
  [url]
  (if-not (str/blank? url)
    [h-box :width "20px" :align :center :children [[hyperlink-href :label "URL" :href url :target "_blank"]]]
    [h-box :width "20px" :align :center :children [[label :width "80px" :label ""]]]))

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
      [popover-anchor-wrapper
       :showing? showing?
       :position @curr-position
       :anchor [h-box :width "200px"
                :children [[:div
                            {:on-mouse-over (handler-fn (reset! showing? true))
                             :on-mouse-out  (handler-fn (reset! showing? false))}
                            tag]]]
       :popover [popover-content-wrapper
                 :body module]])))

(defn format-uuid
  [uuid]
  (let [showing? (reagent/atom false)
        tag (.substring uuid 0 7)]
    (fn []
      [popover-anchor-wrapper
       :showing? showing?
       :position @curr-position
       :anchor [h-box :width "80px"
                :children [[:div
                            {:on-mouse-over (handler-fn (reset! showing? true))
                             :on-mouse-out  (handler-fn (reset! showing? false))}
                            tag]]]
       :popover [popover-content-wrapper
                 :body uuid]])))
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
   :children [[label :width "100px" :label type]
              (service-url serviceUrl)
              [format-uuid uuid]
              [format-module moduleResourceUri]
              [label :width "80px" :label activeVm]
              [label :width "100px" :label status]
              [label :width "200px" :label username]
              [label :width "200px" :label cloudServiceNames]
              [label :width "100px" :label tags]
              [label :width "200px" :label startTime]
              [label :width "300px" :label abort]
              ]])

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
                       :children (vec (map format-run item))]]])))))

(defn runs-panel
  []
  (fn []
    [v-box
     :gap "3px"
     :children [[runs-control]
                [runs-display]]]))
