(ns sixsq.slipstream.authn.main.views
  (:require
    [re-com.core :refer [v-box modal-panel p button]]
    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.authn.main.events]
    [sixsq.slipstream.authn.main.effects]

    [sixsq.slipstream.webui.panel.authn.views :as authn-views]
    [sixsq.slipstream.webui.panel.session.views :as session-views]

    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.widget.history.events]
    [sixsq.slipstream.webui.widget.history.effects]
    [taoensso.timbre :as log]))

(defn message-modal
  []
  (let [message (subscribe [:message])]
    (fn []
      (if @message
        [modal-panel
         :child @message
         :wrap-nicely? true
         :backdrop-on-click #(dispatch [:clear-message])]))))

(defn dispatch-redirect []
  (log/info "dispatching trigger-redirect")
  (dispatch [:evt.authn.main/trigger-redirect]))

(defn login-info
  []
  (let [session (subscribe [:webui.authn/session])]
    (fn []
      (when @session
        (dispatch-redirect)
        [v-box
         :children [[p (str "Authenticated as '" (:username @session) "'.")]
                    [button
                     :label "redirect"
                     :class "btn btn-primary btn-block"
                     :on-click #(dispatch-redirect)]]]))))

(defn resource-panel
  []
  (let [resource-path (subscribe [:resource-path])
        session (subscribe [:webui.authn/session])]
    (fn []
      (when-not @session
        (let [panel (first @resource-path)]
          [v-box
           :class "webui-contents"
           :children [(case panel
                        "login" [authn-views/login-contents]
                        "session" [session-views/session-info]
                        [authn-views/login-contents])]])))))

(defn app []
  [v-box
   :children [[message-modal]
              [login-info]
              [resource-panel]]])
