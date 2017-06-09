(ns sixsq.slipstream.webui.panel.authn.views
  (:require
    [re-com.core :refer [h-box v-box input-text input-password label
                         button info-button modal-panel single-dropdown title line]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils :as utils]
    [taoensso.timbre :as log]))

(defn ordered-params
  "Extracts and orders the parameter descriptions for rendering the form."
  [method]
  (->> method
       :params-desc
       seq
       (sort-by (fn [[_ {:keys [order]}]] order))
       seq))

(defn update-form-data
  [method param-name value]
  (dispatch [:evt.webui.authn/update-form-data [method param-name value]]))

(defn login-button
  "Form login button initiates the login process."
  [{:keys [id label description] :or {id "UNKNOWN_ID", label "UNKNOWN_NAME"} :as method}]
  [h-box
   :gap "0.25ex"
   :justify :between
   :children [[button
               :label label
               :class "btn btn-primary"
               :disabled? false
               :on-click (fn []
                           (.submit (.getElementById js/document (str "login_" id)))
                           (dispatch [:evt.webui.authn/clear-form-data]))]
              [info-button :info description]]])

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
  (let [methods (subscribe [:webui.authn/methods])
        cep (subscribe [:cloud-entry-point])]
    (fn [{:keys [id] :as method}]
      (log/info "creating login form for method" id)
      (let [redirect-uri "/webui/login"
            post-uri (str (:baseURI @cep) (get-in @cep [:sessions :href])) ;; FIXME: Should be part of CIMI API.
            simple-method (second (re-matches #"session-template/(.*)" id))
            params (-> (ordered-params method)
                       (conj ["href" {:displayName "href" :data id}]
                             ["redirectURI" {:displayName "Redirect URI" :data redirect-uri}]))] ;; FIXME: Should come from template
        (when params
          [:form {:id       (str "login_" id)
                  :method   "post"
                  :action   post-uri
                  :enc-type "application/x-www-form-urlencoded"}
           [v-box
            :gap "0.25ex"
            :children (conj (vec (map (partial form-component id) params))
                            [login-button method])]])))))

(defn internal? [{:keys [authn-method]}]
  (= "internal" authn-method))

(defn order-and-group
  "Sorts the methods by ID and then groups them (true/false) on whether it is
   an internal method or not."
  [methods]
  (->> methods
       (sort-by :id)
       (group-by internal?)))

(defn login-form-container
  "Container that holds all of the login forms."
  []
  (let [methods (subscribe [:webui.authn/methods])]
    (fn []
      (let [ordered-methods (order-and-group @methods)
            internals (get ordered-methods true)
            externals (get ordered-methods false)]

        [h-box
         :gap "5ex"
         :align :start
         :children [[v-box
                     :justify :center
                     :gap "2ex"
                     :width "40ex"
                     :children (vec (for [method internals]
                                      [method-form method]))]
                    [v-box
                     :justify :center
                     :gap "2ex"
                     :width "40ex"
                     :children (vec (for [method externals]
                                      [method-form method]))]]]))))

(defn logout-button
  "Buttons shown when the user has an active session to allow the user to view
   her profile or to logout."
  []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [button
       :label (@tr [:logout])
       :on-click #(dispatch [:evt.webui.authn/logout])])))

(defn authn-button
  "Button that navigates to the login page or to the session page depending on
   whether there is an active session or not. When there is no active session,
   the button label is 'login' and redirects to the login page. If there is an
   active session, then the button label is the username and redirects to the
   session page."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (if @session
        (let [button-text (-> @session
                              :username
                              (or "unknown")
                              (utils/truncate 15 "…"))]
          [h-box
           :gap "0.25ex"
           :children [[button :label button-text :on-click #(history/navigate "session")]
                      [logout-button]]])
        (let [button-text (@tr [:login])]
          [button :label button-text :on-click #(history/navigate "login")])))))

(defn login-panel
  "The login panel provides the forms to log into the SlipStream server."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      [v-box
       :children [[title
                   :label (@tr [:login])
                   :level :level1
                   :underline? true]
                  [login-form-container]]])))

