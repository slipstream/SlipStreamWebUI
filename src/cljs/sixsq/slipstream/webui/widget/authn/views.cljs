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
    :on-click #(dispatch [:evt.webui.authn/logout])]])

(defn logout-controls
  []
  (let [tr (subscribe [:i18n-tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (if @session
        (let [{:keys [username]} @session]
          [h-box
           :gap "0.25em"
           :children (logout-buttons @tr (or username "unknown"))])))))

(defn login-controls
  []
  (let [tr (subscribe [:i18n-tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (if-not @session
        [button
         :label (@tr [:login])
         :on-click #(dispatch [:evt.webui.authn/show-dialog true])]))))

(defn ordered-params
  [id methods]
  (->> methods
       (filter #(= id (:id %)))
       first
       :params-desc
       seq
       (sort-by (fn [[_ {:keys [order]}]] order))
       seq))

(defn form-component [method [param-name {:keys [displayName] :as param}]]
  (if (= "Password" displayName)
    [input-password
     :width "100%"
     :model (reagent/atom "")
     :placeholder displayName
     :change-on-blur? true
     :on-change #(dispatch [:evt.webui.authn/update-form-data [method param-name %]])]
    [input-text
     :width "100%"
     :model (reagent/atom "")
     :placeholder displayName
     :change-on-blur? true
     :on-change #(dispatch [:evt.webui.authn/update-form-data [method param-name %]])]))

(defn method-form
  []
  (let [method (subscribe [:webui.authn/method])
        methods (subscribe [:webui.authn/methods])]
    (fn []
      (when-let [params (ordered-params @method @methods)]
        [v-box
         :gap "0.25ex"
         :children (vec (map (partial form-component @method) params))]))))

(defn login-methods-form
  []
  (let [tr (subscribe [:i18n-tr])
        method (subscribe [:webui.authn/method])
        methods (subscribe [:webui.authn/methods])]
    (fn []
      [v-box
       :gap "0.25ex"
       :children [[single-dropdown
                   :choices methods
                   :model @method
                   :placeholder (@tr [:login-method])
                   :width "100%"
                   :on-change #(dispatch [:evt.webui.authn/update-method %])]
                  [method-form]
                  [h-box
                   :gap "0.25ex"
                   :justify :end
                   :children [[button
                               :label (@tr [:cancel])
                               :class "btn-default"
                               :on-click (fn []
                                           (dispatch [:evt.webui.authn/show-dialog false])
                                           (dispatch [:evt.webui.authn/clear-form-data]))]
                              [button
                               :label (@tr [:login])
                               :class "btn-primary"
                               :on-click (fn []
                                           (dispatch [:evt.webui.authn/show-dialog false])
                                           (dispatch [:evt.webui.authn/login]))]]]]])))

(defn login-modal
  []
  (let [show-dialog? (subscribe [:webui.authn/show-dialog?])]
    (fn []
      (if @show-dialog?
        [modal-panel
         :child [v-box
                 :width "35ex"
                 :children [[login-methods-form]]]
         :backdrop-on-click #(dispatch [:evt.webui.authn/show-dialog false])]))))

(defn authn-panel
  []
  [h-box
   :children [[login-controls]
              [logout-controls]]])
