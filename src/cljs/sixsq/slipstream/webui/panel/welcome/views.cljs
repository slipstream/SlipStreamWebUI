(ns sixsq.slipstream.webui.panel.welcome.views
  (:require
    [re-com.core :refer [v-box title]]
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.resource :as resource]))

(defn welcome-resource
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [v-box
       :children [[title
                   :label (@tr [:welcome])
                   :level :level1
                   :underline? true]
                  [:div (@tr [:welcome-text])]]])))

(defmethod resource/render "welcome"
  [path query-params]
  [welcome-resource])
