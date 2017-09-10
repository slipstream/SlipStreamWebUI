(ns sixsq.slipstream.webui.panel.credential.views
  (:require
    [clojure.pprint :refer [pprint]]
    [re-frame.core :refer [subscribe dispatch]]
    [re-com.core :refer [v-box button modal-panel]]
    [sixsq.slipstream.webui.panel.credential.views-table :as table]
    [sixsq.slipstream.webui.panel.credential.effects]
    [sixsq.slipstream.webui.panel.credential.events]
    [sixsq.slipstream.webui.panel.credential.subs]
    [sixsq.slipstream.webui.resource :as resource]
    [sixsq.slipstream.webui.utils-forms :as form-utils]))

(defn credential-forms
  []
  (let [show? (subscribe [:webui.credential/show-modal?])
        descriptions-vector-atom (subscribe [:webui.credential/descriptions-vector])]
    (fn []
      (when @show?
        [modal-panel
         :backdrop-on-click #(dispatch [:evt.webui.credential/hide-modal])
         :child [form-utils/credential-form-container
                 :descriptions descriptions-vector-atom
                 :on-cancel #(dispatch [:evt.webui.credential/hide-modal])
                 :on-submit (fn [data]
                              (dispatch [:evt.webui.credential/create-credential data])
                              (dispatch [:evt.webui.credential/hide-modal]))]]))))

(defn credential-resource
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [v-box
       :gap "2ex"
       :children [[button
                   :class "btn btn-primary"
                   :label (@tr [:create])
                   :on-click #(dispatch [:evt.webui.credential/show-modal])]
                  [credential-forms]
                  [table/credential-resource]]])))

(defmethod resource/render "credential"
  [path query-params]
  [credential-resource])
