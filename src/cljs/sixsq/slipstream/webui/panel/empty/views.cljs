(ns sixsq.slipstream.webui.panel.empty.views
  "Panel containing an empty div. Used as a default rendering page to be shown
   at the start before the initialization is completed."
  (:require
    [re-com.core :refer [v-box]]
    [re-frame.core :refer [dispatch]]))

(defn empty-panel
  []
  (fn []
    (dispatch [:evt.webui.empty/redirect-login])
    [v-box
     :children [[:div]]]))
