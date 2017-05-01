(ns sixsq.slipstream.webui.panel.unknown.views
  (:require
    [clojure.string :as str]
    [re-com.core :refer [v-box title]]
    [re-frame.core :refer [subscribe]]))

(defn unknown-panel
  []
  (let [tr (subscribe [:i18n-tr])
        resource-path (subscribe [:resource-path])]
    (fn []
      (let [path (str "/" (str/join "/" @resource-path))]
        [v-box
         :children [[title
                     :label (@tr [:unknown-resource])
                     :level :level1
                     :underline? true]
                    [:div (@tr [:unknown-resource-text] [path])]]]))))


