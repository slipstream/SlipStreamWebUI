(ns sixsq.slipstream.webui.widget.authn.views
  (:require
    [re-com.core :refer [h-box button]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.panel.authn.views :as authn-views]))

(defn logout-button
  "Button shown when the user has an active session to allow the user to
   logout."
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
                     (let [redirect-page (if @use-modal? "welcome" "login")]
                       (dispatch [:evt.webui.authn/logout])
                       (history/navigate redirect-page)))]))))

(defn login-button
  "Button that sends the user to the login page. The label (and contents of
   the target page) depend on whether the user has an active session. (The
   label is 'login' if no session is active, otherwise it is the username.)"
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
  (let [use-modal? (subscribe [:webui.authn/use-modal?])]
    (fn []
      [h-box
       :gap "0.25ex"
       :children [[login-button]
                  [logout-button]
                  (when @use-modal? [authn-views/login-resource])]])))
