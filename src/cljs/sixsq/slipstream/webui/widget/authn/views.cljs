(ns sixsq.slipstream.webui.widget.authn.views
  (:require
    [re-com.core :refer [h-box button]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [sixsq.slipstream.webui.panel.authn.views :as authn-views]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.utils :as utils]))

(defn logout-button
  "Button shown when the user has an active session to allow the user to
   logout. When the modal is being used, then the user will be redirected
   to the welcome page, otherwise, to the login page."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])
        use-modal? (subscribe [:webui.authn/use-modal?])]
    (fn []
      (when @session
        [button
         :label (@tr [:logout])
         :class "btn-link webui-nav-link"
         :on-click (fn []
                     (dispatch [:evt.webui.authn/logout])
                     (dispatch [:evt.webui.authn/hide-modal])
                     (history/navigate (if @use-modal? "welcome" "login")))]))))

(defn login-button
  "Button that will bring up the login forms.  When the modal is being used,
   it brings up the modal dialog directly.  When not, then the user is sent to
   the login page.

   The label (and contents) depend on whether the user has an active session.
   (The label is 'login' if no session is active, otherwise it is the username.)"
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])
        use-modal? (subscribe [:webui.authn/use-modal?])]
    (fn []
      (let [button-text (if @session
                          (-> @session
                              :username
                              (or "unknown")
                              (utils/truncate 15 "…"))
                          (@tr [:login]))]
        [button
         :label button-text
         :class "btn-link webui-nav-link"
         :on-click (if @use-modal?
                     #(dispatch [:evt.webui.authn/show-modal])
                     #(history/navigate "login"))]))))

(defn authn-buttons
  "Contains two buttons: one to login and one to logout. The login button
   navigates to the 'login' panel. If there is an active session the button
   label will be the username, if not then just 'login'. The panel itself will
   show the login forms or session information as appropriate. The logout
   button will only appear if there is an active session."
  []
  [h-box
   :gap "0.25ex"
   :children [[login-button]
              [logout-button]
              (if @(subscribe [:webui.authn/use-modal?])
                [authn-views/login-resource])]])
