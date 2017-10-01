(ns sixsq.slipstream.webui.panel.app.views
  (:require
    [sixsq.slipstream.webui.components.core :refer [column]]
    [clojure.string :as str]
    [re-com.core :refer [box h-box v-box border hyperlink throbber]]
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
     :on-click #(dispatch [:evt.webui.breadcrumbs/push-breadcrumb module])]))

(defn format-meta [module-meta]
  (let [data (sort-by first (dissoc module-meta :logoLink))]
    (when (pos? (count data))
      [border
       :child [h-box
               :gap "1ex"
               :justify :between
               :children [[h-box
                           :class "webui-column-table"
                           :padding "1ex 1ex 1ex 1ex"
                           :gap "1ex"
                           :children [[column
                                       :model data
                                       :key-fn first
                                       :value-fn (comp name first)
                                       :value-class "webui-row-header"]
                                      [column
                                       :model data
                                       :key-fn first
                                       :value-fn second]]]
                          (when-let [{:keys [logoLink]} module-meta]
                            [box
                             :width "30ex"
                             :style {:background-image    (str "url(\"" logoLink "\")")
                                     :background-size     "contain"
                                     :background-repeat   "no-repeat"
                                     :background-position "50% 50%"}
                             :child [:div]])]]])))

(defn module-resource []
  (let [data (subscribe [:modules-data])]
    (fn []
      (if @data
        (let [module-meta (dissoc @data :children)
              module-children (:children @data)]
          [v-box
           :gap "3ex"
           :children [[format-meta module-meta]
                      [v-box
                       :children (doall (map format-module module-children))]]])
        [v-box
         :children [[throbber]]]))))

(defmethod resource/render "application"
  [path query-params]
  (dispatch [:evt.webui.app/modules-search])
  [module-resource])
