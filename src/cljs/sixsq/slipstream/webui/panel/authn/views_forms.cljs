(ns sixsq.slipstream.webui.panel.authn.views-forms
  (:require
    [re-com.core :refer [h-box v-box box input-text input-password label alert-box progress-bar
                         button info-button modal-panel single-dropdown title line gap]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.panel.authn.utils :as u]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [sixsq.slipstream.webui.panel.authn.views-session :as session]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils :as utils]
    [taoensso.timbre :as log]))

(defn hidden? [{:keys [type] :as param-desc}]
  (= "hidden" type))

(defn ordered-params
  "Extracts and orders the parameter descriptions for rendering the form.
   Returns a tuple with two ordered parameter groups. The first contains the
   list of hidden parameters; the second contains the list of visible ones."
  [method]
  (let [params (->> method
                    :params-desc
                    seq
                    (sort-by (fn [[_ {:keys [order]}]] order))
                    (group-by (fn [[k v]] (hidden? v))))]
    [(get params true) (get params false)]))

(defn update-form-data
  [method param-name value]
  (dispatch [:evt.webui.authn/update-form-data [method param-name value]]))

(defn form-component
  "Provides a single element of a form. This should provide a reasonable
   control for each defined type, but this initial implementation just provides
   either a text or password field. The changed data is stored in the global
   database."
  [method [param-name {:keys [data type displayName] :as param}]]
  (case type
    "hidden" [:input {:name  param-name
                      :type  "hidden"
                      :value (or data "")}]
    "password" [input-password
                :attr {:name param-name}
                :width "100%"
                :model (reagent/atom (or data ""))
                :placeholder displayName
                :change-on-blur? true
                :on-change #(update-form-data method param-name %)]
    [input-text
     :attr {:name param-name}
     :width "100%"
     :model (reagent/atom (or data ""))
     :placeholder displayName
     :change-on-blur? true
     :on-change #(update-form-data method param-name %)]))

(defn login-button
  "Button to initiate the login process by submitting the form associated with
   the login method. This also clears the form data and any error message."
  [{:keys [id label group description] :or {id "UNKNOWN_ID", label "UNKNOWN_NAME"} :as method}]
  [box
   :class "webui-block-button"
   :size "auto"
   :child [button
           :label (or group label)
           :class "btn btn-primary btn-block"
           :disabled? false
           :on-click (fn []
                       (.submit (.getElementById js/document (str "login_" id)))
                       (dispatch [:evt.webui.authn/clear-form-data])
                       (dispatch [:evt.webui.authn/clear-error-message]))]])

(defn method-form
  "Renders the form for a particular login method. The fields are taken from
   the login method description."
  []
  (let [methods (subscribe [:webui.authn/methods])
        cep (subscribe [:cloud-entry-point])
        redirect-uri (subscribe [:webui.authn/redirect-uri])
        use-modal? (subscribe [:webui.authn/use-modal?])
        path (subscribe [:resource-path])]
    (fn [{:keys [id] :as method}]
      (log/info "creating login form for method" id)
      (let [{:keys [baseURI collection-href]} @cep
            redirect-uri (if @use-modal?
                           (str/join "/" (into ["" "webui"] @path))
                           @redirect-uri)
            post-uri (str baseURI (:sessions collection-href)) ;; FIXME: Should be part of CIMI API.
            simple-method (second (re-matches #"session-template/(.*)" id))
            [hidden-params visible-params] (ordered-params method)
            hidden-params (conj
                            hidden-params
                            ["href" {:displayName "href" :data id :type "hidden"}]
                            ["redirectURI" {:displayName "redirectURI" :data redirect-uri :type "hidden"}])]

        (log/info "using redirect url" redirect-uri)
        [:form {:id       (str "login_" id)
                :method   "post"
                :action   post-uri
                :enc-type "application/x-www-form-urlencoded"}
         [v-box
          :children [[v-box :children (vec (map (partial form-component id) hidden-params))]
                     (when (pos? (count visible-params))
                       [v-box
                        :gap "0.5ex"
                        :children (conj (vec (map (partial form-component id) visible-params))
                                        [gap :size "1ex"])])
                     [login-button method]]]]))))

(defn model-login-forms
  []
  (let [show? (reagent/atom false)]
    (fn [method-type methods]
      [v-box
       :width "35ex"
       :children [[button
                   :label method-type
                   :class "btn btn-primary btn-block"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [v-box
                             :gap "1ex"
                             :width "35ex"
                             :children (conj (vec (doall (for [method methods]
                                                           [method-form method])))
                                             [gap :size "2ex"]
                                             [button
                                              :label (str "Cancel")
                                              :class "btn btn-danger btn-block"
                                              :on-click #(reset! show? false)])]])]])))

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

(defn sort-value [[tag [{:keys [authn-method]}]]]
  (if (= "internal" authn-method)
    "internal"
    (or tag authn-method)))

(defn order-and-group
  "Sorts the methods by ID and then groups them (true/false) on whether it is
   an internal method or not."
  [methods]
  (->> methods
       (sort-by :id)
       (group-by (or :group :authn-method))
       (sort-by sort-value method-comparator)))

(defn login-form-group
  "Renders the forms for a given group. If there's only one item, the form is
   rendered directly. If not, the forms are rendered in a modal dialog and a
   button activates that dialog."
  []
  (fn [method-type methods]
    (if (= 1 (count methods))
      [v-box
       :width "35ex"
       :children [[method-form (first methods)]]]
      [model-login-forms method-type methods])))

(defn login-form-container
  "Container that holds all of the login forms. These will be placed into two
   columns. The first has the 'internal' login forms and the second contains
   all of the rest. An extra margin will be added to each column so that the
   contents are spaced reasonably when they flow. This margin should be removed
   from the container holding the returned h-box."
  []
  (let [methods (subscribe [:webui.authn/methods])
        total (subscribe [:webui.authn/total])
        count (subscribe [:webui.authn/count])
        wrapped-row-spacing "6ex"
        margin (str "0 3ex " wrapped-row-spacing " 3ex")
        neg-margin (str "0 3ex -" wrapped-row-spacing " 3ex")]
    (fn []
      (let [methods (order-and-group @methods)
            internals (first methods)
            externals (rest methods)
            progress (if (zero? @total) 0 (int (* 100. (/ @count @total))))]
        [v-box
         :style {:margin neg-margin}
         :children [(if (or (zero? @total) (not= @count @total))
                      [progress-bar :model progress]
                      [h-box
                       :align :start
                       :justify :center
                       :class "webui-wrap"
                       :style {:flex-flow "row wrap"}
                       :children [[v-box
                                   :style {:margin margin}
                                   :children [[login-form-group (first internals) (second internals)]]]
                                  [v-box
                                   :gap "2ex"
                                   :style {:margin margin}
                                   :children (vec (map (fn [[k v]] [login-form-group k v]) externals))]]])]]))))

(defn error-message
  "Provides the error message as an alert box when the message isn't nil."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        error-message (subscribe [:webui.authn/error-message])]
    (fn []
      (when @error-message
        [alert-box
         :alert-type :danger
         :heading (@tr [:login-failed])
         :body @error-message
         :closeable? true
         :on-close #(dispatch [:evt.webui.authn/clear-error-message])]))))

(defn login-controls
  []
  [v-box
   :children [[error-message]
              [login-form-container]]])

