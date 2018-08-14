(ns sixsq.slipstream.webui.history.views
  (:require
    [re-frame.core :refer [dispatch]]
    [sixsq.slipstream.webui.history.events :as history-events]))


(defn link
  "Renders a link that will navigate to the given href when clicked. The href
   value will also be used as the label, unless an explicit label is provided."
  [href & [label]]
  [:a {:on-click #(dispatch [::history-events/navigate href])
       :style    {:cursor "pointer"}}
   (or label href)])
