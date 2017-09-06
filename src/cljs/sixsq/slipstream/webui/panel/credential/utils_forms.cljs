(ns sixsq.slipstream.webui.panel.credential.utils-forms
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
  [template-id param-name value]
  (dispatch [:evt.webui.credential/update-form-data template-id param-name value]))

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

(defn credential-template-form
  [description]
  (let [methods (subscribe [:webui.authn/methods])
        cep (subscribe [:cloud-entry-point])
        redirect-uri (subscribe [:webui.authn/redirect-uri])]
    (fn [{:keys [id] :as description}]
      (log/info "creating credential template for" id)
      (let [{:keys [baseURI collection-href]} @cep
            [hidden-params visible-params] (ordered-params description)]
        (log/error (with-out-str (cljs.pprint/pprint hidden-params)))
        (log/error (with-out-str (cljs.pprint/pprint visible-params)))

        [v-box
         :children [(when (pos? (count hidden-params))
                      [v-box
                       :children (vec (map (partial form-component id) hidden-params))])
                    (when (pos? (count visible-params))
                      [v-box
                       :gap "0.5ex"
                       :children (vec (map (partial form-component id) visible-params))])]]))))

(defn action-buttons
  []
  (let [form-data (subscribe [:webui.credential/form-data])]
    (fn []
      [h-box
       :justify :between
       :children [[button
                   :class "btn btn-default"
                   :label "cancel"
                   :on-click #(dispatch [:evt.webui.credential/hide-modal])]
                  [button
                   :class "btn btn-primary"
                   :label "create"
                   :on-click (fn []
                               (dispatch [:evt.webui.credential/create-credential @form-data])
                               (dispatch [:evt.webui.credential/hide-modal]))]]])))

(defn credential-form-container
  []
  (let [descriptions-atom (subscribe [:webui.credential/descriptions])
        selected-id (reagent/atom nil)]
    (fn []
      (let [descriptions (sort :id (vals @descriptions-atom))]
        [v-box
         :width "40ex"
         :gap "2ex"
         :children [[single-dropdown
                     :choices descriptions
                     :model selected-id
                     :on-change (fn [new-id]
                                  (reset! selected-id new-id)
                                  (dispatch [:evt.webui.credential/update-form-data new-id nil nil]))
                     :placeholder "type of credential"
                     :style {:min-width "40ex"}]
                    [credential-template-form (get @descriptions-atom @selected-id)]
                    [action-buttons]]]))))

(defn modal-form
  []
  (let [show? (subscribe [:webui.credential/show-modal?])]
    (fn []
      (when @show?
        [modal-panel
         :backdrop-on-click #(dispatch [:evt.webui.credential/hide-modal])
         :child [credential-form-container]]))))

(defn credential-forms
  []
  [v-box
   :children [[modal-form]]])

