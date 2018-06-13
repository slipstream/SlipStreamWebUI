(ns sixsq.slipstream.webui.authn.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [taoensso.timbre :as log]
    [reagent.core :as r]

    [sixsq.slipstream.webui.authn.events :as authn-events]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.cimi.subs :as cimi-subs]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.history.utils :as history-utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.general :as utils]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]))


(defn method-comparator
  "Compares two login method types. The value 'internal' will always compare
   as less than anything other than itself."
  [x y]
  (cond
    (= x y) 0
    (= "internal" x) -1
    (= "internal" y) 1
    (< x y) -1
    :else 1))


(defn sort-value [[tag [{:keys [method]}]]]
  (if (= "internal" method)
    "internal"
    (or tag method)))


(defn order-and-group
  "Sorts the methods by ID and then groups them (true/false) on whether it is
   an internal method or not."
  [methods]
  (->> methods
       (sort-by :id)
       (group-by #(or (:group %) (:method %)))
       (sort-by sort-value method-comparator)))


(defn internal-or-api-key
  [[_ methods]]
  (let [authn-method (:method (first methods))]
    (#{"internal" "api-key"} authn-method)))


(defn self-registration
  [[_ methods]]
  (let [authn-method (:method (first methods))]
    (#{"self-registration"} authn-method)))


(defn hidden? [{:keys [type] :as param-desc}]
  (= "hidden" type))


(defn ordered-params
  "Extracts and orders the parameter descriptions for rendering the form."
  [method]
  (->> method
       :params-desc
       seq
       (sort-by (fn [[_ {:keys [order]}]] order))))


(defn keep-param-mandatory-not-readonly? [[k {:keys [mandatory readOnly]}]]
  (and mandatory (not readOnly)))


(defn select-method-by-id
  [id methods]
  (->> methods
       (filter #(= id (:id %)))
       first))


(defn form-component
  "Provides a single element of a form. This should provide a reasonable
   control for each defined type, but this initial implementation just provides
   either a text or password field."
  [[param-name {:keys [data type displayName mandatory] :as param}]]
  (case type
    "hidden" [ui/FormField [:input {:name param-name :type "hidden" :value (or data "")}]]
    "password" [ui/FormInput {:name         param-name
                              :type         type
                              :placeholder  displayName
                              :icon         "lock"
                              :iconPosition "left"
                              :required     mandatory}]
    [ui/FormInput {:name         param-name
                   :type         type
                   :placeholder  displayName
                   :icon         "user"
                   :iconPosition "left"
                   :required     mandatory}]))


(defn authn-method-form
  "Renders the form for a particular authentication (login or sign up) method.
   The fields are taken from the method description."
  [method-type methods collections-kw id-prefix]
  (let [cep (subscribe [::cimi-subs/cloud-entry-point])
        selected-method-group (r/atom (when (= 1 (count methods)) (first methods)))
        server-redirect-uri (subscribe [::authn-subs/server-redirect-uri])]
    (fn [method-type methods collections-kw id-prefix]
      (let [dropdown? (> (count methods) 1)
            {:keys [id label] :as method} @selected-method-group
            {:keys [baseURI collection-href]} @cep
            id (or id method-type)
            post-uri (str baseURI (collections-kw collection-href)) ;; FIXME: Should be part of CIMI API.
            inputs-method (conj (->> method ordered-params (filter keep-param-mandatory-not-readonly?))
                                ["href" {:displayName "href" :data id :type "hidden"}]
                                ["redirectURI" {:displayName "redirectURI" :data @server-redirect-uri :type "hidden"}])]
        (log/infof "creating authentication form: %s %s" (name collections-kw) id)
        (vec (concat [ui/Form {:id     (str id-prefix id)
                               :action post-uri
                               :method "post"}]
                     (map form-component inputs-method)
                     [(if-not dropdown?
                        [ui/FormButton {:primary true :fluid true} label]
                        [ui/FormField
                         [ui/ButtonGroup {:primary true, :fluid true}
                          [ui/Button {:disabled (not @selected-method-group)} method-type]
                          [ui/Dropdown
                           {:options       (map #(identity {:key   (:id %)
                                                            :text  (:label %)
                                                            :value (:id %)}) methods)
                            :button        true
                            :class-name    "icon"
                            :close-on-blur true
                            :onChange      #(let [id (-> (js->clj %2 :keywordize-keys true) :value)
                                                  selected-method (select-method-by-id id methods)]
                                              (reset! selected-method-group selected-method))
                            :style         {:text-align "center"}}]]])]))))))


(defn login-method-form
  [method-type methods]
  [authn-method-form method-type methods :sessions "login_"])


(defn signup-method-form
  [method-type methods]
  [authn-method-form method-type methods :users "signup_"])


(defn authn-form-container
  "Container that holds all of the authentication (login or sign up) forms.
   These will be placed into two columns. The first has the 'internal' login
   forms and the second contains all of the rest."
  [collection-kw failed-kw group-fn method-form-fn]
  (let [template-href (cimi-utils/template-href collection-kw)
        templates (subscribe [::cimi-subs/collection-templates (keyword template-href)])
        loading? (subscribe [::cimi-subs/collection-templates-loading? (keyword template-href)])
        tr (subscribe [::i18n-subs/tr])
        error-message (subscribe [::authn-subs/error-message])]
    (fn [collection-kw failed-kw group-fn method-form-fn]
      (let [method-groups (order-and-group (-> @templates :templates vals))
            internals (filter group-fn method-groups)
            externals (remove group-fn method-groups)
            externals? (empty? externals)]

        [ui/Segment {:basic true}
         (when @error-message
           [ui/Message {:negative  true
                        :size      "tiny"
                        :onDismiss #(dispatch [::authn-events/clear-error-message])}
            [ui/MessageHeader (@tr [failed-kw])]
            [:p @error-message]])

         (if @loading?
           [ui/Dimmer {:active true :inverted true} [ui/Loader (@tr [:loading])]]
           [ui/Grid {:columns 2 :textAlign "center" :stackable true}

            [ui/GridColumn {:stretched true}
             [ui/Segment {:basic externals? :textAlign "left"}
              (vec (concat [:div]
                           (map (fn [[k v]] [method-form-fn k v]) internals)))]]

            (when-not externals?
              [ui/GridColumn {:stretched true}
               [ui/Segment {:textAlign "left"}
                [:div
                 (vec (concat [:div]
                              (map (fn [[k v]] [method-form-fn k v]) externals)))]]])])]))))


(defn login-form-container
  []
  [authn-form-container :session :login-failed internal-or-api-key login-method-form])


(defn signup-form-container
  []
  [authn-form-container :user :signup-failed self-registration signup-method-form])


(defn authn-modal
  "Modal that holds the authentication (login or sign up) forms."
  [id label form-fn]
  (let [tr (subscribe [::i18n-subs/tr])
        open-modal (subscribe [::authn-subs/open-modal])]
    (fn [id label form-fn]
      [ui/Modal
       {:id        id
        :open      (= @open-modal label)
        :closeIcon true
        :on-close  #(dispatch [::authn-events/close-modal])}
       [ui/ModalHeader (@tr [label])]
       [ui/ModalContent {:scrolling true}
        [form-fn]]])))


(defn modal-login []
  [authn-modal "modal-login-id" :login login-form-container])


(defn modal-signup []
  [authn-modal "modal-signup-id" :signup signup-form-container])


(defn login-menu
  "This panel shows the login button and modal (if open)."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        template-href (cimi-utils/template-href :user)
        user-templates (subscribe [::cimi-subs/collection-templates (keyword template-href)])]
    (fn []
      [:div
       [ui/ButtonGroup {:primary true :size "tiny"}
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


(defn authn-menu
  "Provides either a login or user dropdown depending on whether the user has
   an active session. The login button will bring up a modal dialog."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        user (subscribe [::authn-subs/user])
        on-click (fn []
                   (dispatch [::authn-events/logout])
                   (dispatch [::history-events/navigate "welcome"]))]
    (fn []
      (if-not @user
        [login-menu]
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
            :on-click #(history-utils/navigate "profile")}]
          [ui/DropdownItem
           {:key      "sign-out"
            :text     (@tr [:logout])
            :icon     "sign out"
            :on-click on-click}]]]))))


(defn ^:export open-authn-modal []
  (log/debug "dispatch open-modal for authn view")
  (dispatch [::authn-events/open-modal :login]))
