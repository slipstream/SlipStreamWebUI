(ns sixsq.slipstream.webui.authn.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.authn.effects]
    [sixsq.slipstream.webui.authn.events]
    [sixsq.slipstream.webui.authn.subs]
    [sixsq.slipstream.webui.history :as history]))

(defn logout-buttons
  [tr user-id]
  [[button
    :label user-id
    :on-click #(history/navigate "profile")]
   [button
    :label (tr [:logout])
    :on-click #(dispatch [:logout])]])

(defn logout
  []
  (let [tr (subscribe [:i18n-tr])
        authn (subscribe [:authn])]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if logged-in?
          [h-box
           :children (logout-buttons @tr (or user-id "unknown"))])))))

(defn login
  []
  (let [tr (subscribe [:i18n-tr])
        authn (subscribe [:authn])
        username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if-not logged-in?
          [h-box
           :gap "3px"
           :children [[input-text
                       :model username
                       :placeholder (@tr [:username])
                       :change-on-blur? true
                       :on-change #(reset! username %)]
                      [input-password
                       :model password
                       :placeholder (@tr [:password])
                       :change-on-blur? true
                       :on-change #(reset! password %)]
                      [button
                       :label (@tr [:login])
                       :on-click (fn []
                                   (dispatch [:login {:username @username :password @password}])
                                   (reset! username "")
                                   (reset! password ""))]]])))))

(defn authn-panel []
  [h-box
   :children [[login]
              [logout]]])
