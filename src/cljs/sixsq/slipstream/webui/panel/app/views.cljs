(ns sixsq.slipstream.webui.panel.app.views
  (:require
    [re-com.core :refer [h-box v-box label hyperlink]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.panel.app.effects]
    [sixsq.slipstream.webui.panel.app.events]
    [sixsq.slipstream.webui.panel.app.subs]

    [sixsq.slipstream.webui.widget.i18n.subs]

    [sixsq.slipstream.webui.widget.breadcrumbs.views :as crumbs-views]
    [sixsq.slipstream.webui.widget.breadcrumbs.events]
    [sixsq.slipstream.webui.resource :as resource]))

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

(defn module-resource []
  [v-box
   :gap "1ex"
   :children [[crumbs-views/breadcrumbs]
              [module-listing]]])

(defmethod resource/render "application"
  [path query-params]
  [module-resource])
