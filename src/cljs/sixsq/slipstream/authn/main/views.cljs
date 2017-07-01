(ns sixsq.slipstream.authn.main.views
  (:require
    [re-com.core :refer [v-box modal-panel title]]
    [re-frame.core :refer [subscribe dispatch]]
    [taoensso.timbre :as log]

    [sixsq.slipstream.authn.main.events]
    [sixsq.slipstream.webui.panel.authn.views-forms :as forms]
    [sixsq.slipstream.webui.panel.authn.views-session :as session]))

(defn redirect
  []
  (log/info "dispatching trigger-redirect")
  (dispatch [:evt.authn.main/trigger-redirect]))

(defn message-modal
  []
  (let [message (subscribe [:message])]
    (fn []
      (if @message
        [modal-panel
         :child @message
         :wrap-nicely? true
         :backdrop-on-click #(dispatch [:clear-message])]))))

(defn app
  []
  (fn []
    (let [session (subscribe [:webui.authn/session])]
      (fn []
        (when @session (redirect))
        [v-box
         :children [[message-modal]
                    (when @session [title
                                    :label "Redirecting..."
                                    :level :level1
                                    :underline? true])
                    (if @session
                      [session/session-info]
                      [forms/login-controls])]]))))
