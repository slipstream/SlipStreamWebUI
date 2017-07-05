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
    [sixsq.slipstream.webui.utils :as utils]))

(defn logout-button
  "Button shown when the user has an active session to allow the user to
   logout."
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [button
       :label (@tr [:logout])
       :class "btn-link webui-nav-link"
       :on-click (fn []
                   (dispatch [:evt.webui.authn/logout])
                   (history/navigate "login"))])))

(defn authn-buttons
  "Contains two buttons: one to login and one to logout. The login button
   navigates to the 'login' panel. If there is an active session the button
   label will be the username, if not then just 'login'. The panel itself will
   show the login forms or session information as appropriate. The logout
   button will only appear if there is an active session."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (let [button-text (if @session
                          (-> @session
                              :username
                              (or "unknown")
                              (utils/truncate 15 "…"))
                          (@tr [:login]))]
        [h-box
         :gap "0.25ex"
         :children [[button
                     :label button-text
                     :class "btn-link webui-nav-link"
                     :on-click #(history/navigate "login")]
                    (when @session [logout-button])]]))))
