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

(defn runs-panel
  []
  (let [runs-data (subscribe [:runs-data])]
    (fn []
      [v-box
       :gap "3px"
       :children [[runs-control]
                  (if @runs-data
                    [scroller
                     :scroll :auto
                     :width "500px"
                     :height "300px"
                     :child [v-box
                             :children [[title
                                         :label "RUNS"
                                         :level :level3
                                         :underline? true]
                                        (str @runs-data)]]])
                  ]])))
