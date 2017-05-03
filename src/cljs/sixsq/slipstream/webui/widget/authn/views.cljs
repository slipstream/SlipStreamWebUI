(ns sixsq.slipstream.webui.widget.authn.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.widget.history.utils :as history]

    [sixsq.slipstream.webui.widget.authn.effects]
    [sixsq.slipstream.webui.widget.authn.events]
    [sixsq.slipstream.webui.widget.authn.subs]))

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
           :gap "0.25em"
           :children (logout-buttons @tr (or user-id "unknown"))])))))

(defn login-button
  []
  (let [tr (subscribe [:i18n-tr])
        authn (subscribe [:authn])]
    (fn []
      (let [{:keys [logged-in?]} @authn]
        (if-not logged-in?
          [button
           :label (@tr [:login])
           :on-click #(dispatch [:open-login-dialog])])))))

(defn login-form
  []
  (let [tr (subscribe [:i18n-tr])
        authn (subscribe [:authn])
        username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if-not logged-in?
          [v-box
           :gap "0.25em"
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
                      [h-box
                       :justify :end
                       :gap "0.25em"
                       :children [[button
                                   :label (@tr [:cancel])
                                   :on-click (fn []
                                               (dispatch [:close-login-dialog])
                                               (reset! username "")
                                               (reset! password ""))]
                                  [button
                                   :label (@tr [:login])
                                   :class "btn-primary"
                                   :on-click (fn []
                                               (dispatch [:login {:username @username :password @password}])
                                               (reset! username "")
                                               (reset! password ""))]]]]])))))

(defn login-modal
  []
  (let [show-login-dialog? (subscribe [:show-login-dialog?])]
    (fn []
      (if @show-login-dialog?
        [modal-panel
         :child [v-box
                 :children [[login-form]]]
         :backdrop-on-click #(dispatch [:close-login-dialog])]))))

(defn authn-panel
  []
  [h-box
   :children [[login-button]
              [logout]]])
