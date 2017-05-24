(ns sixsq.slipstream.webui.panel.authn.views
  (:require
    [re-com.core :refer [h-box v-box input-text input-password label
                         button modal-panel single-dropdown title]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils :as utils]))

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

(defn update-form-data
  [method param-name value]
  (dispatch [:evt.webui.authn/update-form-data [method param-name value]]))

(defn form-component
  "Provides a single element of a form. This should provide a reasonable
   control for each defined type, but this initial implementation just provides
   either a text or password field. The changed data is stored in the global
   database."
  [method [param-name {:keys [data type displayName] :as param}]]
  (cond
    (or (= "Password" displayName) (= "password" type)) [input-password
                                                         :attr {:name param-name}
                                                         :width "100%"
                                                         :model (reagent/atom (or data ""))
                                                         :placeholder displayName
                                                         :change-on-blur? true
                                                         :on-change #(update-form-data method param-name %)]
    (or (= "redirectURI" param-name) (= "href" param-name)) [:input {:name  param-name
                                                                     :type  "hidden"
                                                                     :value (or data "")}]
    :else [input-text
           :attr {:name param-name}
           :width "100%"
           :model (reagent/atom (or data ""))
           :placeholder displayName
           :change-on-blur? true
           :on-change #(update-form-data method param-name %)]))

(defn method-form
  "Renders the form for a particular login method. The fields are taken from
   the login method description."
  []
  (let [method (subscribe [:webui.authn/method])
        methods (subscribe [:webui.authn/methods])
        cep (subscribe [:cloud-entry-point])]
    (fn []
      (let [redirect-uri "/webui/login"
            post-uri (str (:baseURI @cep) (get-in @cep [:sessions :href])) ;; FIXME: Should be part of CIMI API.
            simple-method (second (re-matches #"session-template/(.*)" (str @method)))
            params (-> (ordered-params @method @methods)
                       (conj ["href" {:displayName "href" :data @method}]
                             ["redirectURI" {:displayName "Redirect URI" :data redirect-uri}]))] ;; FIXME: Should come from template
        (when params
          [:form {:id       (str "login_" @method)
                  :method   "post"
                  :action   post-uri
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

(defn login-button
  "Form login button initiates the login process."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        method (subscribe [:webui.authn/method])]
    (fn []
      [button
       :label (@tr [:login])
       :class "btn-primary"
       :disabled? (nil? @method)
       :on-click (fn []
                   (.submit (.getElementById js/document (str "login_" @method)))
                   (dispatch [:evt.webui.authn/clear-form-data]))])))

(defn login-forms
  "Allows the user to select the login method, supply information, and then
   login. The form is different for each login method."
  []
  [v-box
   :gap "0.25ex"
   :width "35ex"
   :children [[login-dropdown]
              [method-form]
              [login-button]]])

(defn login-form
  "Modal dialog to allow the user to choose the login method and to provide
   the necessary information."
  []
  (let [session (subscribe [:webui.authn/session])]
    (fn []
      (when-not @session
        [v-box
         :justify :center
         :children [[login-forms]]]))))

(defn logout-button
  "Buttons shown when the user has an active session to allow the user to view
   her profile or to logout."
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [button
       :class "btn-primary"
       :label (@tr [:logout])
       :on-click #(dispatch [:evt.webui.authn/logout])])))

(defn authn-button
  "Button that navigates to the login page. The text of the button is either
   'login' or the username depending on whether there is an active session or
   not."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (let [{:keys [username] :or {:username "unknown"}} @session
            button-text (if @session (utils/truncate username 15 "…") (@tr [:login]))]
        [button :label button-text :on-click #(history/navigate "login")]))))

(defn column
  [vs cls]
  [v-box
   :class "webui-column"
   :children (doall (for [v vs] [label :class cls :label v]))])

(defn session-table [session]
  (let [data (sort (u/remove-common-attrs session))
        ks (map (comp name first) data)
        vs (map (comp str second) data)]
    [h-box
     :children [[column ks "webui-row-header"]
                [column vs ""]]]))

(defn session-info
  [session]
  (when session
    [v-box
     :children [[session-table session]
                [logout-button]]]))

(defn login-panel
  "The login panel provides the forms to log into the SlipStream server if the
   user is not already logged in. If the user is logged in, then the current
   session information is provided, as well as a logout button."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      [v-box
       :children [[title
                   :label (@tr (if @session [:session] [:login]))
                   :level :level1
                   :underline? true]
                  [login-form]
                  [session-info @session]]])))

