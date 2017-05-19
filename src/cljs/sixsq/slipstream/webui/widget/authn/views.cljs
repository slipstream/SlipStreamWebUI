(ns sixsq.slipstream.webui.widget.authn.views
  (:require
    [re-com.core :refer [h-box v-box input-text input-password
                         button modal-panel single-dropdown]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.widget.history.utils :as history]

    [sixsq.slipstream.webui.widget.authn.effects]
    [sixsq.slipstream.webui.widget.authn.events]
    [sixsq.slipstream.webui.widget.authn.subs]))

(defn ordered-params
  "Selects the information for a single login method and then orders the
   parameter descriptions for rendering the form."
  [id methods]
  (->> methods
       (filter #(= id (:id %)))
       first
       :params-desc
       seq
       (sort-by (fn [[_ {:keys [order]}]] order))
       seq))

(defn form-component
  "Provides a single element of a form. This should provide a reasonable
   control for each defined type, but this initial implementation just provides
   either a text or password field. The changed data is stored in the global
   database."
  [method [param-name {:keys [data displayName] :as param}]]
  (if (= "Password" displayName)
    [input-password
     :attr {:name param-name}
     :width "100%"
     :model (reagent/atom (or data ""))
     :placeholder displayName
     :change-on-blur? true
     :on-change #(dispatch [:evt.webui.authn/update-form-data [method param-name %]])]
    [input-text
     :attr {:name param-name}
     :width "100%"
     :model (reagent/atom (or data ""))
     :placeholder displayName
     :change-on-blur? true
     :on-change #(dispatch [:evt.webui.authn/update-form-data [method param-name %]])]))

(defn method-form
  "Renders the form for a particular login method. The fields are taken from
   the login method description."
  []
  (let [method (subscribe [:webui.authn/method])
        methods (subscribe [:webui.authn/methods])
        cep (subscribe [:cloud-entry-point])]
    (fn []
      (let [redirect-uri "/webui/profile"
            simple-method (second (re-matches #"session-template/(.*)" (str @method)))
            params (ordered-params @method @methods)
            params (if (seq params)                         ;; FIXME: Get this from template description.
                     params
                     [["redirectURI" {:displayName "Redirect URI" :data redirect-uri}]
                      ["href" {:displayName "href" :data @method}]])]
        (when params
          [:form {:id (str "login_" @method)
                  :method "post"
                  :action "/api/session"
                  :enc-type "application/x-www-form-urlencoded"}
           [v-box
            :gap "0.25ex"
            :children (vec (map (partial form-component @method) params))]])))))

(defn login-dropdown
  "Dropdown that contains the list of available login methods."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        method (subscribe [:webui.authn/method])
        methods (subscribe [:webui.authn/methods])]
    (fn []
      [single-dropdown
       :choices methods
       :model @method
       :placeholder (@tr [:login-method])
       :width "100%"
       :on-change #(dispatch [:evt.webui.authn/update-method %])])))

(defn cancel-button
  "Form login button that cancels the login process and clears any entered
   data."
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [button
       :label (@tr [:cancel])
       :class "btn-default"
       :on-click (fn []
                   (dispatch [:evt.webui.authn/show-dialog false])
                   (dispatch [:evt.webui.authn/clear-form-data]))])))

(defn login-button
  "Form login button that clears the modal and initiates the login process."
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [button
       :label (@tr [:login])
       :class "btn-primary"
       :on-click (fn []
                   (.submit (.getElementById js/document "login_session-template/github"))
                   (dispatch [:evt.webui.authn/show-dialog false]))])))

(defn login-forms
  "Allows the user to select the login method, supply information, and then
   login (or cancel). The form is different for each login method."
  []
  [v-box
   :gap "0.25ex"
   :children [[login-dropdown]
              [method-form]
              [h-box
               :gap "0.25ex"
               :justify :end
               :children [[cancel-button]
                          [login-button]]]]])

(defn login-modal
  "Modal dialog to allow the user to choose the login method and to provide
   the necessary information."
  []
  (let [shown? (subscribe [:webui.authn/show-dialog?])]
    (fn []
      (if @shown?
        [modal-panel
         :child [v-box
                 :width "35ex"
                 :children [[login-forms]]]
         :backdrop-on-click #(dispatch [:evt.webui.authn/show-dialog false])]))))

(defn logout-controls
  "Buttons shown when the user has an active session to allow the user to view
   her profile or to logout."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (if @session
        (let [{:keys [username]} @session]
          [h-box
           :gap "0.25em"
           :children [[button
                       :label (or username "unknown")
                       :on-click #(history/navigate "profile")]
                      [button
                       :label (@tr [:logout])
                       :on-click #(dispatch [:evt.webui.authn/logout])]]])))))

(defn login-controls
  "Button shown when the user does not have an active session to allow the
   user to log in."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (if-not @session
        [button
         :label (@tr [:login])
         :on-click #(dispatch [:evt.webui.authn/show-dialog true])]))))

(defn authn-widget
  "Small widget that shows the appropriate log in/out buttons depending on
   whether the user has an active session."
  []
  [h-box
   :children [[login-controls]
              [logout-controls]]])
