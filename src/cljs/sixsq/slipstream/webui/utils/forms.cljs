(ns sixsq.slipstream.webui.utils.forms
  (:require
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


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


(defn template-form
  [form-data-atom {:keys [id] :as description}]
  (let [[hidden-params visible-params] (ordered-params description)
        update-data-fn (partial update-data form-data-atom)
        form-component-fn (partial ff/form-field update-data-fn id)]
    (vec (map form-component-fn (concat hidden-params visible-params)))))


(defn descriptions->options [descriptions]
  (vec (map (fn [{:keys [id label]}] {:value id, :text (or label id)}) descriptions)))


(defn template-selector
  [update-form-data-fn selected-id-atom descriptions]
  [ui/FormField
   [ui/FormSelect
    {:fluid     true
     :value     @selected-id-atom
     :label     "resource template"
     :options   (descriptions->options descriptions)
     :on-change (ui-callback/value (fn [value]
                                     (reset! selected-id-atom value)
                                     (update-form-data-fn value nil nil)))}]])


(defn form-container-inner-modal
  [show? templates on-submit on-cancel]
  (let [tr (subscribe [::i18n-subs/tr])
        selected-id (reagent/atom nil)
        form-data (reagent/atom nil)]
    (fn [show? templates on-submit on-cancel]
      (when (nil? @selected-id)
        (reset! selected-id (:id (first (sort-by :id templates))))
        (update-data form-data @selected-id nil nil))
      (let [templates (sort-by :id templates)
            selected-description (first (filter #(= @selected-id (:id %)) templates))]
        [ui/Modal {:open      show?
                   :onClose   on-cancel
                   :closeIcon true}
         [ui/ModalHeader (@tr [:create])]
         [ui/ModalContent {:scrolling true}
          (vec (concat [ui/Form]
                       [[template-selector
                         (partial update-data form-data)
                         selected-id
                         templates]]
                       (template-form form-data selected-description)))]
         [ui/ModalActions
          [ui/Button
           {:on-click on-cancel}
           (@tr [:cancel])]
          [ui/Button
           {:primary  true
            :on-click #(on-submit @form-data)}
           (@tr [:create])]]]))))


(defn form-container-inner-modal-single
  [show? template on-submit on-cancel]
  (let [tr (subscribe [::i18n-subs/tr])
        form-data (reagent/atom nil)]
    (fn [show? template on-submit on-cancel]
      (update-data form-data (:id template) nil nil)
      [ui/Modal {:open      show?
                 :onClose   on-cancel
                 :closeIcon true}
       [ui/ModalHeader (@tr [:create])]
       [ui/ModalContent {:scrolling true}
        (vec (concat [ui/Form]
                     (template-form form-data template)))]
       [ui/ModalActions
        [ui/Button
         {:on-click on-cancel}
         (@tr [:cancel])]
        [ui/Button
         {:primary  true
          :on-click #(on-submit @form-data)}
         (@tr [:create])]]])))
;;
;; public component
;;

(defn form-container-modal
  [& {:keys [show? templates on-submit on-cancel]
      :or   {on-submit #()
             on-cancel #()}
      :as   args}]
  [form-container-inner-modal show? templates on-submit on-cancel])

(defn form-container-modal-single-template
  [& {:keys [show? template on-submit on-cancel]
      :or   {on-submit #()
             on-cancel #()}
      :as   args}]
  [form-container-inner-modal-single show? template on-submit on-cancel])
