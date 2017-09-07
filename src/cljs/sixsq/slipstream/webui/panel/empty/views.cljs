(ns sixsq.slipstream.webui.panel.empty.views
  "Panel containing an empty div. Used as a default rendering page to be shown
   at the start before the initialization is completed."
  (:require
    [re-com.core :refer [v-box]]
    [re-frame.core :refer [dispatch]]

    [sixsq.slipstream.webui.panel.empty.events]
    [sixsq.slipstream.webui.resource :as resource]))

(defn empty-panel
  []
  (fn []
    [v-box
     :children [[:div]]]))

(defmethod resource/render "empty"
  [path query-params]
  [empty-panel])

(defmethod resource/render nil
  [path query-params]
  [empty-panel])
