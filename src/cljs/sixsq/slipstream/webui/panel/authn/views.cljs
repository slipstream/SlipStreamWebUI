(ns sixsq.slipstream.webui.panel.authn.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [re-com.core :refer [v-box title modal-panel]]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [sixsq.slipstream.webui.panel.authn.views-session :as session]
    [sixsq.slipstream.webui.panel.authn.views-forms :as forms]
    [sixsq.slipstream.webui.resource :as resource]))

(defn login-resource
  "This panel shows the login controls if there is no active user session;
   otherwise it shows the details of the session."
  []
  (let [session (subscribe [:webui.authn/session])
        use-modal? (subscribe [:webui.authn/use-modal?])
        show-modal? (subscribe [:webui.authn/show-modal?])
        error-message (subscribe [:webui.authn/error-message])]
    (fn []
      (if @error-message
        (if @session
          (dispatch [:evt.webui.authn/clear-error-message])
          (dispatch [:evt.webui.authn/show-modal])))
      (if @use-modal?
        (when @show-modal?
          [modal-panel
           :backdrop-on-click #(dispatch [:evt.webui.authn/hide-modal])
           :child (if @session
                    [session/session-info]
                    [forms/login-controls])])
        [v-box
         :children [(if @session
                      [session/session-info]
                      [forms/login-controls])]]))))

(defmethod resource/render "login"
  [path query-params]
  (dispatch [:set-login-path-and-error (:error query-params)])
  [login-resource])
