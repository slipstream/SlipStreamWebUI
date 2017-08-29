(ns sixsq.slipstream.webui.panel.authn.views-session
  (:require
    [re-com.core :refer [h-box v-box label button title]]
    [re-frame.core :refer [subscribe]]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.subs]))

(defn column
  [vs cls]
  [v-box
   :class "webui-column"
   :children (doall (for [v vs] [label :class cls :label v]))])

(defn session-info
  "Provides the user's session information."
  []
  (let [session (subscribe [:webui.authn/session])]
    (fn []
      (let [data (sort (u/remove-common-attrs @session))
            ks (map (comp name first) data)
            vs (map (comp str second) data)]
        [h-box
         :children [[column ks "webui-row-header"]
                    [column vs ""]]]))))

