(ns sixsq.slipstream.webui.panel.unknown.views
  (:require
    [clojure.string :as str]
    [re-com.core :refer [v-box title]]
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.main.subs]
    [sixsq.slipstream.webui.resource :as resource]))

(defn unknown-resource
  []
  (let [tr (subscribe [:webui.i18n/tr])
        resource-path (subscribe [:webui.main/nav-path])]
    (fn []
      (let [path (str "/" (str/join "/" @resource-path))]
        [v-box
         :children [[title
                     :label (@tr [:unknown-resource])
                     :level :level1
                     :underline? true]
                    [:div (@tr [:unknown-resource-text] [path])]]]))))

(defmethod resource/render :default
  [path query-params]
  [unknown-resource])
