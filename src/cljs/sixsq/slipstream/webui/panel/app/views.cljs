(ns sixsq.slipstream.webui.panel.app.views
  (:require
    [clojure.string :as str]
    [re-com.core :refer [v-box hyperlink]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.panel.app.effects]
    [sixsq.slipstream.webui.panel.app.events]
    [sixsq.slipstream.webui.panel.app.subs]
    [sixsq.slipstream.webui.resource :as resource]
    [sixsq.slipstream.webui.widget.breadcrumbs.events]
    [taoensso.timbre :as log]))

(defn format-module [module]
  (when module
    [hyperlink
     :label module
     :on-click #(dispatch [:push-breadcrumb module])]))

(defn module-resource []
  (let [data (subscribe [:modules-data])]
    (fn []
      [v-box :children (doall (map format-module @data))])))

(defmethod resource/render "application"
  [path query-params]
  (dispatch [:modules-search])
  [module-resource])
