(ns sixsq.slipstream.webui.panel.welcome.views
  (:require
    [clojure.string :as str]
    [re-com.core :refer [v-box title]]
    [re-frame.core :refer [subscribe]]))

(defn welcome-panel
  []
  (let [tr (subscribe [:i18n-tr])]
    (fn []
      [v-box
       :children [[title
                   :label (@tr [:welcome])
                   :level :level1
                   :underline? true]
                  [:div (@tr [:welcome-text])]]])))


