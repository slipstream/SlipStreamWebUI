(ns sixsq.slipstream.scui.authn.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.scui.authn.effects]
    [sixsq.slipstream.scui.authn.events]
    [sixsq.slipstream.scui.authn.subs]))

(defn logout-buttons
  [user-id]
  [[button
    :label user-id
    :on-click #(js/alert (str "profile for " user-id))]
   [button
    :label "logout"
    :on-click #(dispatch [:logout])]])

(defn logout
  []
  (let [authn (subscribe [:authn])]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if logged-in?
          [h-box
           :children (logout-buttons (or user-id "unknown"))])))))

(defn login
  []
  (let [authn (subscribe [:authn])
        username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if-not logged-in?
          [h-box
           :gap "3px"
           :children [[input-text
                       :model username
                       :placeholder "username"
                       :change-on-blur? true
                       :on-change #(reset! username %)]
                      [input-password
                       :model password
                       :placeholder "password"
                       :change-on-blur? true
                       :on-change #(reset! password %)]
                      [button
                       :label "login"
                       :on-click (fn []
                                   (dispatch [:login {:username @username :password @password}])
                                   (reset! username "")
                                   (reset! password ""))]]])))))

(defn authn-panel []
  [h-box
   :children [[login]
              [logout]]])
