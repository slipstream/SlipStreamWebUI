(ns sixsq.slipstream.webui.utils.forms
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe]]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.component :as ui-utils]))


(defn hidden?
  "Determines if the given parameter description describes a hidden field."
  [{:keys [type] :as param-desc}]
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


(defn update-data [form-data-atom form-id param value]
  (let [data (cond-> {:href form-id}
                     param (merge {param value}))]
    (if (= form-id (:href @form-data-atom))
      (swap! form-data-atom merge data)
      (reset! form-data-atom data))))


(defn credential-template-form
  [form-data-atom {:keys [id] :as description}]
  (let [[hidden-params visible-params] (ordered-params description)
        update-data-fn (partial update-data form-data-atom)
        form-component-fn (partial ff/form-field update-data-fn id)]
    (vec (map form-component-fn (concat hidden-params visible-params)))))


(defn descriptions->options [descriptions]
  (vec (map (fn [{:keys [id label]}] {:value id, :text label}) descriptions)))


(defn credential-template-selector
  [update-form-data-fn selected-id-atom descriptions]
  [ui/FormField
   [ui/FormSelect
    {:fluid     true
     :value     @selected-id-atom
     :label     "resource template"
     :options   (descriptions->options descriptions)
     :on-change (ui-utils/callback :value
                                   (fn [value]
                                     (reset! selected-id-atom value)
                                     (update-form-data-fn value nil nil)))}]])


(defn credential-form-container-inner-modal
  [show? descriptions-atom on-submit on-cancel]
  (let [tr (subscribe [::i18n-subs/tr])
        selected-id (reagent/atom nil)
        form-data (reagent/atom nil)]
    (fn []
      (when (nil? @selected-id)
        (reset! selected-id (:id (first (sort-by :id @descriptions-atom)))))
      (let [descriptions (sort-by :id @descriptions-atom)
            selected-description (first (filter #(= @selected-id (:id %)) descriptions))]
        [ui/Modal
         {:size "tiny"
          :open @show?}
         [ui/ModalContent
          {:scrolling true}
          (vec (concat [ui/Form]
                       [[credential-template-selector
                         (partial update-data form-data)
                         selected-id
                         descriptions]]
                       (credential-template-form form-data selected-description)))]
         [ui/ModalActions
          [ui/Button
           {:on-click on-cancel}
           (@tr [:cancel])]
          [ui/Button
           {:primary  true
            :on-click #(on-submit @form-data)}
           (@tr [:create])]]]))))

;;
;; public component
;;

(defn credential-form-container-modal
  [& {:keys [show? descriptions on-submit on-cancel]
      :or   {on-submit #()
             on-cancel #()}
      :as   args}]
  [credential-form-container-inner-modal show? descriptions on-submit on-cancel])
