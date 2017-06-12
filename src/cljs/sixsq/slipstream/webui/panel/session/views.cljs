(ns sixsq.slipstream.webui.panel.session.views
  (:require
    [re-com.core :refer [h-box v-box label button title]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.subs]))

(defn column
  [vs cls]
  [v-box
   :class "webui-column"
   :children (doall (for [v vs] [label :class cls :label v]))])

(defn session-table [session]
  (let [data (sort (u/remove-common-attrs session))
        ks (map (comp name first) data)
        vs (map (comp str second) data)]
    [h-box
     :children [[column ks "webui-row-header"]
                [column vs ""]]]))

(defn session-info
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (if @session
        [session-table @session]
        [label :label (@tr [:no-session])]))))

(defn session-panel
  "This panel shows the details of the user's active session. If there is no
   active session, then a simple message to that effect is shown."
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [v-box
       :children [[title
                   :label (@tr [:session])
                   :level :level1
                   :underline? true]
                  [session-info]]])))

