(ns sixsq.slipstream.webui.authn.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [sixsq.slipstream.webui.authn.events :as authn-events]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.authn.utils :as u]
    [sixsq.slipstream.webui.cimi.subs :as cimi-subs]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.history.utils :as history-utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.general :as utils]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))


(defn form-component
  "Provides a single element of a form. This should provide a reasonable
   control for each defined type, but this initial implementation just provides
   either a text or password field."
  [[param-name {:keys [data type displayName mandatory autocomplete] :as param}]]
  (case type
    "hidden" [ui/FormField [:input {:name param-name :type "hidden" :value (or data "")}]]
    "password" [ui/FormInput {:name         param-name
                              :type         type
                              :placeholder  displayName
                              :icon         "lock"
                              :iconPosition "left"
                              :required     mandatory
                              :autoComplete (or autocomplete "off")}]
    [ui/FormInput {:name         param-name
                   :type         type
                   :placeholder  displayName
                   :icon         "user"
                   :iconPosition "left"
                   :required     mandatory
                   :autoComplete (or autocomplete "off")}]))


(defn dropdown-method-option
  [{:keys [id label] :as method}]
  {:key id, :text label, :value id})


(defn authn-method-form
  "Renders the form for a particular authentication (login or sign up) method.
   The fields are taken from the method description."
  [methods collections-kw]
  (let [cep (subscribe [::cimi-subs/cloud-entry-point])
        server-redirect-uri (subscribe [::authn-subs/server-redirect-uri])
        form-id (subscribe [::authn-subs/form-id])]
    (fn [methods collections-kw]
      (let [dropdown? (> (count methods) 1)
            method (u/select-method-by-id @form-id methods)

            {:keys [baseURI collection-href]} @cep
            post-uri (str baseURI (collections-kw collection-href)) ;; FIXME: Should be part of CIMI API.
            inputs-method (conj (->> method u/ordered-params (filter u/keep-visible-params))
                                [:href {:displayName "href" :data @form-id :type "hidden"}]
                                [:redirectURI {:displayName "redirectURI" :data @server-redirect-uri :type "hidden"}])

            dropdown-options (map dropdown-method-option methods)]

        (log/infof "creating authentication form: %s %s" (name collections-kw) @form-id)
        (vec
          (concat
            [ui/Form {:id (or @form-id "authn-form-placeholder-id"), :action post-uri, :method "post"}]

            [(vec (concat [ui/Segment {:style {:height "35ex"}}
                           (when dropdown?
                             [ui/FormDropdown
                              {:options       dropdown-options
                               :value         @form-id
                               :fluid         true
                               :selection     true

                               :close-on-blur true
                               :on-change     (ui-callback/dropdown ::authn-events/set-form-id)}])]

                          (mapv form-component inputs-method)))]))))))


(defn login-method-form
  [[_ methods]]
  [authn-method-form methods :sessions])


(defn signup-method-form
  [[_ methods]]
  [authn-method-form methods :users])


(defn authn-method-group-option
  [[group _]]
  {:text group, :value group})


(defn authn-method-dropdown
  [method-groups]
  (let [selected-method-group (subscribe [::authn-subs/selected-method-group])]
    (fn [method-groups]
      (let [default (ffirst method-groups)
            default-form-id (-> method-groups first second first :id)
            options (mapv authn-method-group-option method-groups)]

        (when (nil? @selected-method-group)
          (dispatch [::authn-events/set-selected-method-group default])
          (dispatch [::authn-events/set-form-id default-form-id]))

        [ui/Dropdown {:fluid     true
                      :selection true
                      :loading   (nil? @selected-method-group)
                      :value     @selected-method-group
                      :options   options
                      :on-change (ui-callback/value
                                   (fn [group-id]
                                     (dispatch [::authn-events/set-selected-method-group group-id])
                                     (let [form-id (-> (u/select-group-methods-by-id group-id method-groups)
                                                       first
                                                       :id)]
                                       (dispatch [::authn-events/set-form-id form-id]))))}]))))


(defn authn-form-container
  "Container that holds all of the authentication (login or sign up) forms."
  [collection-kw failed-kw group-fn method-form-fn]
  (let [template-href (cimi-utils/template-href collection-kw)
        templates (subscribe [::cimi-subs/collection-templates (keyword template-href)])
        tr (subscribe [::i18n-subs/tr])
        error-message (subscribe [::authn-subs/error-message])
        selected-method-group (subscribe [::authn-subs/selected-method-group])]
    (fn [collection-kw failed-kw classifier-fn group-form-fn]
      (let [authn-method-groups (u/grouped-authn-methods @templates classifier-fn)
            selected-authn-method-group (some->> authn-method-groups
                                                 (filter #(-> % first (= @selected-method-group)))
                                                 first)]

        [ui/Segment {:basic true}
         (when @error-message
           [ui/Message {:negative  true
                        :size      "tiny"
                        :onDismiss #(dispatch [::authn-events/clear-error-message])}
            [ui/MessageHeader (@tr [failed-kw])]
            [:p @error-message]])

         [authn-method-dropdown authn-method-groups]
         [ui/Divider]
         [group-form-fn selected-authn-method-group]]))))


(defn login-form-container
  []
  [authn-form-container :session :login-failed u/internal-or-api-key login-method-form])


(defn signup-form-container
  []
  [authn-form-container :user :signup-failed u/self-registration signup-method-form])


(defn switch-panel-link
  [modal-kw]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [modal-kw]
      (let [other-modal (case modal-kw
                          :login :signup
                          :signup :login)
            f (fn []
                (dispatch [::authn-events/close-modal])
                (dispatch [::authn-events/set-selected-method-group nil])
                (dispatch [::authn-events/set-form-id nil])
                (dispatch [::authn-events/open-modal other-modal]))]
        (case modal-kw
          :login [:span (@tr [:no-account?]) " " [:a {:on-click f} (str (@tr [:signup-link]))]]
          :signup [:span (@tr [:already-registered?]) " " [:a {:on-click f} (str (@tr [:login-link]))]])))))


(defn authn-modal
  "Modal that holds the authentication (login or sign up) forms."
  [id modal-kw form-fn]
  (let [tr (subscribe [::i18n-subs/tr])
        open-modal (subscribe [::authn-subs/open-modal])
        form-id (subscribe [::authn-subs/form-id])]
    (fn [id modal-kw form-fn]
      [ui/Modal
       {:id        id
        :size      :tiny
        :open      (= @open-modal modal-kw)
        :closeIcon true
        :on-close  #(dispatch [::authn-events/close-modal])}

       [ui/ModalHeader (@tr [modal-kw])]

       [ui/ModalContent
        [form-fn]]

       [ui/ModalActions
        [switch-panel-link modal-kw]
        [ui/Button {:positive true
                    :disabled (nil? @form-id)
                    :on-click #(some->> @form-id
                                        (.getElementById js/document)
                                        (.submit))}
         (@tr [modal-kw])]]])))


(defn modal-login []
  [authn-modal "modal-login-id" :login login-form-container])


(defn modal-signup []
  [authn-modal "modal-signup-id" :signup signup-form-container])


(defn login-menu
  "This menu provides a login button and a dropdown with more options (like
   sign up). The login and sign up actions bring up modal dialogs."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        template-href (cimi-utils/template-href :user)
        user-templates (subscribe [::cimi-subs/collection-templates (keyword template-href)])]
    (fn []
      [:div
       [ui/ButtonGroup {:primary true, :size "tiny"}
        [ui/Button {:on-click #(dispatch [::authn-events/open-modal :login])}
         [ui/Icon {:name "sign in"}] (@tr [:login])]
        [ui/Dropdown {:inline    true
                      :button    true
                      :pointing  "top right"
                      :className "icon"}
         (vec
           (concat
             [ui/DropdownMenu]
             (when
               (get-in @user-templates [:templates (keyword (str template-href "/self-registration"))])
               [[ui/DropdownItem {:icon     "signup"
                                  :text     (@tr [:signup])
                                  :on-click #(dispatch [::authn-events/open-modal :signup])}]
                [ui/DropdownDivider]])
             [[ui/DropdownItem {:icon   "book"
                                :text   (@tr [:documentation])
                                :href   "http://ssdocs.sixsq.com/"
                                :target "_blank"}]
              [ui/DropdownItem {:icon   "info circle"
                                :text   (@tr [:knowledge-base])
                                :href   "http://support.sixsq.com/solution/categories"
                                :target "_blank"}]
              [ui/DropdownItem {:icon "mail"
                                :text (@tr [:support])
                                :href (str "mailto:support%40sixsq%2Ecom?subject=%5BSlipStream%5D%20Support%20"
                                           "question%20%2D%20Not%20logged%20in")}]]))]]
       [modal-login]
       [modal-signup]])))


(defn user-menu
  "Provides the user dropdown menu providing access to the user profile and an
   action to logout."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        user (subscribe [::authn-subs/user])
        profile-fn #(history-utils/navigate "profile")
        sign-out-fn (fn []
                      (dispatch [::authn-events/logout])
                      (dispatch [::history-events/navigate "welcome"]))]
    (fn []
      [ui/Dropdown {:item            true
                    :simple          false
                    :icon            nil
                    :close-on-change true
                    :trigger         (r/as-element [:span [ui/Icon {:name "user circle"}]
                                                    (utils/truncate @user)])}
       [ui/DropdownMenu
        [ui/DropdownItem
         {:key      "profile"
          :text     (@tr [:profile])
          :icon     "user"
          :on-click profile-fn}]
        [ui/DropdownItem
         {:key      "sign-out"
          :text     (@tr [:logout])
          :icon     "sign out"
          :on-click sign-out-fn}]]])))


(defn authn-menu
  "Provides either a login or user dropdown depending on whether the user has
   an active session. The login button will bring up a modal dialog."
  []
  (let [user (subscribe [::authn-subs/user])]
    (fn []
      (if @user
        [user-menu]
        [login-menu]))))


(defn ^:export open-authn-modal []
  (log/debug "dispatch open-modal for login modal")
  (dispatch [::authn-events/open-modal :login]))


(defn ^:export open-sign-up-modal []
  (log/debug "dispatch open-modal for sign up modal")
  (dispatch [::authn-events/open-modal :signup]))
