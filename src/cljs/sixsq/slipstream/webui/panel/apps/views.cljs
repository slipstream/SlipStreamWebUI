(ns sixsq.slipstream.webui.panel.apps.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.panel.apps.effects]
    [sixsq.slipstream.webui.panel.apps.events]
    [sixsq.slipstream.webui.panel.apps.subs]))

(defn format-crumb [s index]
  [hyperlink
   :label (str s)
   :on-click #(dispatch [:trim-breadcrumbs index])])

(defn breadcrumbs []
  (let [tr (subscribe [:i18n-tr])
        crumbs (subscribe [:modules-breadcrumbs])]
    (fn []
      (let [home (@tr [:home])]
        [h-box
         :gap "3px"
         :children
         (doall (interpose [label :label ">"] (map format-crumb (cons home @crumbs) (range))))]))))

(defn format-module [module]
  (if module [hyperlink
              :label module
              :on-click #(dispatch [:push-breadcrumb module])]))

(defn module-listing
  []
  (let [data (subscribe [:modules-data])]
    (fn []
      [v-box
       :children (doall (map format-module @data))])))

(defn modules-panel []
  [v-box
   :gap "3px"
   :children [[breadcrumbs]
              [module-listing]]])
