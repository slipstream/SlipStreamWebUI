(ns sixsq.slipstream.webui.activity.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.activity.effects]
    [sixsq.slipstream.webui.activity.events]
    [sixsq.slipstream.webui.activity.subs]))

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
  (if url
    [hyperlink-href :label "url" :href url :target "_blank"]
    [label :label ""]))

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
   :children [[label :label uuid]
              (service-url serviceUrl)
              [label :label activeVm]
              [label :label status]
              [label :label username]
              [label :label cloudServiceNames]
              [label :label moduleResourceUri]
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
