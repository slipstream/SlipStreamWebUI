(ns sixsq.slipstream.webui.apps.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.apps.effects]
    [sixsq.slipstream.webui.apps.events]
    [sixsq.slipstream.webui.apps.subs]))

(defn modules-control []
  (let [path (reagent/atom "")]
    (fn []
      [h-box
       :gap "3px"
       :children [[input-text
                   :model path
                   :placeholder "module"
                   :width "150px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! path v)
                                (dispatch [:set-module-path (if (str/blank? v) nil v)]))]
                  [button
                   :label "show modules"
                   :on-click #(dispatch [:modules-search])]]])))

(defn format-crumb [index s]
  [hyperlink
   :label s
   :on-click (dispatch [:trim-breadcrumbs index])])

(defn breadcrumbs []
  (let [crumbs (subscribe [:modules-breadcrumbs])]
    (fn []
      [h-box
       :children
       (doall (interpose [label :label ">"] (map format-crumb (cons "Home" @crumbs) (range 0))))])))

(defn format-module [module]
  [hyperlink
   :label module
   :on-click (dispatch [:push-breadcrumb module])])

(defn modules-panel
  []
  (let [modules-data (subscribe [:modules-data])]
    (fn []
      [v-box
       :gap "3px"
       :children [[modules-control]
                  [breadcrumbs]
                  (if @modules-data
                    [scroller
                     :scroll :auto
                     :width "500px"
                     :height "300px"
                     :child [v-box
                             :children (doall (map format-module @modules-data))]]
                    (dispatch [:modules-breadcrumbs-search]))]])))
