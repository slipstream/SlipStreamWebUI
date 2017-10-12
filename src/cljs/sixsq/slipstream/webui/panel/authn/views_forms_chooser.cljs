(ns sixsq.slipstream.webui.panel.authn.views-forms-chooser
  (:require
    [re-com.core :refer [h-box v-box box input-text input-password label alert-box progress-bar
                         button info-button modal-panel single-dropdown title line gap throbber]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [sixsq.slipstream.webui.panel.authn.views-session :as session]
    [sixsq.slipstream.webui.panel.authn.utils-forms :as form-utils]
    [sixsq.slipstream.webui.utils :as utils]))

(defn login-button
  "Button to initiate the login process by submitting the form associated with
   the login method. This also clears the form data and any error message."
  [{:keys [id] :or {id "UNKNOWN_ID"} :as method}]
  [h-box
   :gap "1ex"
   :justify :between
   :children [[button
               :label "cancel"
               :class "btn btn-default"
               :disabled? false
               :on-click (fn []
                           (dispatch [:evt.webui.authn/clear-form-data])
                           (dispatch [:evt.webui.authn/clear-error-message])
                           (dispatch [:evt.webui.authn/hide-modal]))]
              [button
               :label "login"
               :class "btn btn-primary"
               :disabled? false
               :on-click (fn []
                           (.submit (.getElementById js/document (str "login_" id)))
                           (dispatch [:evt.webui.authn/clear-form-data])
                           (dispatch [:evt.webui.authn/clear-error-message]))]]])

(defn method-form
  "Renders the form for a particular login method. The fields are taken from
   the login method description."
  []
  (let [methods (subscribe [:webui.authn/methods])
        cep (subscribe [:webui.main/cloud-entry-point])
        redirect-uri (subscribe [:webui.authn/redirect-uri])]
    (fn [{:keys [id] :as method}]
      (let [{:keys [baseURI collection-href]} @cep
            redirect-uri @redirect-uri
            post-uri (str baseURI (:sessions collection-href)) ;; FIXME: Should be part of CIMI API.
            simple-method (second (re-matches #"session-template/(.*)" id))
            [hidden-params visible-params] (form-utils/ordered-params method)
            hidden-params (conj
                            hidden-params
                            ["href" {:displayName "href" :data id :type "hidden"}]
                            ["redirectURI" {:displayName "redirectURI" :data redirect-uri :type "hidden"}])]
        [:div
         (when-let [desc (:description method)]
           [:p {:style {:color "grey"}} desc])
         [:form {:id       (str "login_" id)
                 :method   "post"
                 :action   post-uri
                 :enc-type "application/x-www-form-urlencoded"}
          [v-box
           :children [[v-box :children (vec (map (partial form-utils/form-component id) hidden-params))]
                      (when (pos? (count visible-params))
                        [v-box
                         :gap "0.5ex"
                         :children (conj (vec (map (partial form-utils/form-component id) visible-params))
                                         [gap :size "1ex"])])]]]
         [login-button method]]))))

(defn method-label
  [method]
  (or (:group method)
      (:authn-method method)))

(defn group-methods
  [methods]
  (group-by method-label methods))

(defn dropdown-choice
  [[label methods]]
  {:id      label
   :label   label
   :methods methods})

(defn method-group-choices
  [grouped-methods]
  (map dropdown-choice grouped-methods))

(defn select-method-by-id
  [id methods]
  (->> methods
       (filter #(= id (:id %)))
       first))

(defn login-form-chooser
  [{:keys [methods] :as selected-method-group}]
  (if (= 1 (count methods))
    [v-box
     :width "35ex"
     :children [[method-form (first methods)]]]
    (let [choices (method-group-choices (group-methods methods))
          selected-method-id (atom (-> methods first :id))
          selection-method-group (select-method-by-id @selected-method-id methods)]
      [v-box
       :align :start
       :justify :center
       :style {:min-width "35ex"}
       :gap "2ex"
       :children [[single-dropdown
                   :style {:min-width "35ex"}
                   :model selected-method-id
                   :group-fn (constantly nil)
                   :choices methods
                   :placeholder "login group"
                   :on-change #(reset! selected-method-id %)]
                  (when selection-method-group
                      [v-box
                       :width "35ex"
                       :children [[method-form selection-method-group]]])]])))

(defn login-group-chooser
  []
  (let [methods (subscribe [:webui.authn/methods])
        selected-method-group-id (reagent/atom nil)]
    (fn []
      (let [choices (method-group-choices (group-methods @methods))
            selected-method-group (select-method-by-id @selected-method-group-id choices)]
        [v-box
         :align :start
         :justify :center
         :style {:min-width "35ex"}
         :gap "2ex"
         :children [[single-dropdown
                     :style {:min-width "35ex"}
                     :model selected-method-group-id
                     :group-fn (constantly nil)
                     :choices choices
                     :placeholder "login method"
                     :on-change #(reset! selected-method-group-id %)]
                    (when selected-method-group
                      (login-form-chooser selected-method-group))]]))))

(defn login-form-container
  "Container that holds all of the login forms."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        loading? (subscribe [:webui.authn/loading?])]
    (fn []
      [v-box
       :style {:min-width        "35ex"
               :min-height       "15ex"
               :background-color "white"}
       :justify :center
       :gap "1ex"
       :children [[h-box
                   :children [[title
                               :level :level2
                               :label (@tr [:login])
                               :margin-top "0"
                               :underline? false]]]
                  (if @loading?
                    [throbber
                     :size :large
                     :color "grey"]
                    [v-box
                     :align :start
                     :justify :center
                     :children [[login-group-chooser]]])]])))
