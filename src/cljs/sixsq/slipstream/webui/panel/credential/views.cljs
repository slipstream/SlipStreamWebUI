(ns sixsq.slipstream.webui.panel.credential.views
  (:require
    [clojure.pprint :refer [pprint]]
    [re-frame.core :refer [subscribe dispatch]]
    [re-com.core :refer [v-box button]]
    [sixsq.slipstream.webui.panel.credential.views-table :as table]
    [sixsq.slipstream.webui.panel.credential.effects]
    [sixsq.slipstream.webui.panel.credential.events]
    [sixsq.slipstream.webui.panel.credential.subs]
    [sixsq.slipstream.webui.panel.credential.utils-forms :as utils-forms]
    [sixsq.slipstream.webui.resource :as resource]))

(defn credential-resource
  []
  (let [descriptions (subscribe [:webui.credential/descriptions])]
    (fn []
      [v-box
       :gap "2ex"
       :children [[button
                   :class "btn btn-primary"
                   :label "Add"
                   :on-click #(dispatch [:evt.webui.credential/show-modal])]
                  [utils-forms/credential-forms]
                  [table/cimi-resource]]])))

(defmethod resource/render "credential"
  [path query-params]
  [credential-resource])
