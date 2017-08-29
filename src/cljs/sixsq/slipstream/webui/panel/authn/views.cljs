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

(defn login-children []
  (let [session (subscribe [:webui.authn/session])]
    (fn []
      (if @session
        [session/session-info]
        [forms/login-controls]))))

(defn login-resource
  "This panel shows the login controls if there is no active user session;
   otherwise it shows the details of the session."
  []
  (let [use-modal? (subscribe [:webui.authn/use-modal?])
        show-modal? (subscribe [:webui.authn/show-modal?])]
    (fn []
      (let [use-modal? @use-modal?
            show-modal? @show-modal?]
        (if use-modal?
          (when show-modal?
            [modal-panel
             :class "webui-modal"
             :backdrop-on-click #(dispatch [:evt.webui.authn/hide-modal])
             :child [v-box
                     :children [[login-children]]]])
          [v-box
           :children [[login-children]]])))))

(defmethod resource/render "login"
  [path query-params]
  (dispatch [:set-login-path-and-error (:error query-params)])
  [login-resource])
