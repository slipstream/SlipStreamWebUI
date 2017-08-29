(ns sixsq.slipstream.webui.panel.empty.views
  "Panel containing an empty div. Used as a default rendering page to be shown
   at the start before the initialization is completed."
  (:require
    [re-com.core :refer [v-box label throbber]]
    [re-frame.core :refer [dispatch]]

    [sixsq.slipstream.webui.resource :as resource]))

(defn empty-panel
  []
  (fn []
    [v-box
     :align :center
     :children [[label
                 :label "Loading..."
                 :style {:font-size :large}]
                [throbber
                 :size :large
                 :color "blue"]]]))

(defmethod resource/render "empty"
  [path query-params]
  [empty-panel])

(defmethod resource/render nil
  [path query-params]
  [empty-panel])
