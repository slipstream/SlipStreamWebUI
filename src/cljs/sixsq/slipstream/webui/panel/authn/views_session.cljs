(ns sixsq.slipstream.webui.panel.authn.views-session
  (:require
    [re-com.core :refer [h-box v-box label button title]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.components.core :refer [column]]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.subs]))

(defn session-info
  "Provides the user's session information."
  []
  (let [session @(subscribe [:webui.authn/session])
        data (sort (u/remove-common-attrs session))
        key-fn (comp name first)
        value-fn (comp str second)]
    [h-box
     :gap "1ex"
     :children [[column
                 :model data
                 :key-fn key-fn
                 :value-fn key-fn
                 :value-class "webui-row-header"]
                [column
                 :model data
                 :key-fn key-fn
                 :value-fn value-fn]]]))

