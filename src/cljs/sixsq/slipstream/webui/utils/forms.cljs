(ns sixsq.slipstream.webui.utils.forms
  (:require
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.docs.subs :as docs-subs]
    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.form-fields-resource-metadata :as new-ff]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]))


(defn on-return-key
  "Will execute the given no-arg function when the value of k is the
   value for the return key (13). Before executing the function it will
   blur the active element in the document, ignoring any errors."
  [f k]
  (when (and f (= (.-charCode k) 13))
    (try
      (some-> js/document .-activeElement .blur)
      (catch :default _ nil))
    (f)))


(defn hidden?
  "Determines if the given parameter description describes a hidden field."
  [{:keys [type] :as param-desc}]
  (= "hidden" type))


(defn ordered-params
  "Orders the parameter of resource metadata for rendering the form."
  [resource-metadata]
  (sort-by :order resource-metadata))


(defn update-data [form-data-atom form-id param value]
  (let [data (cond-> {:href form-id}
                     param (merge {param value}))]
    (if (= form-id (:href @form-data-atom))
      (swap! form-data-atom merge data)
      (reset! form-data-atom data))))


(defn template-form                                         ;; FIXME: filter should be a reusable function
  [form-data-atom {:keys [id] :as template} resourceMetadata]
  (let [update-data-fn (partial update-data form-data-atom)
        form-component-fn (partial new-ff/form-field update-data-fn id)
        attributes (->> (:attributes resourceMetadata)
                        (filter (fn [{:keys [consumerWritable consumerMandatory] :as attribute}]
                                  (or consumerWritable
                                      (and (not consumerWritable)
                                           consumerMandatory))))
                        (sort-by :order))]
    (mapv form-component-fn attributes)))


(defn descriptions->options [descriptions]
  (mapv (fn [{:keys [id label]}] {:value id, :text (or label id)}) descriptions))


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
        (reset! selected-id (:id (first templates)))
        (update-data form-data @selected-id nil nil))
      (let [selected-template (first (filter #(= @selected-id (:id %)) templates))
            resourceMetadata (subscribe [::docs-subs/document selected-template])]
        [ui/Modal {:open      show?
                   :onClose   on-cancel
                   :closeIcon true}
         [ui/ModalHeader (@tr [:create])]
         [ui/ModalContent
          [ui/ModalDescription
           (vec (concat [ui/Form]
                        [[template-selector
                          (partial update-data form-data)
                          selected-id
                          templates]]
                        (template-form form-data selected-template @resourceMetadata)))]]
         [ui/ModalActions
          [uix/Button
           {:text     (@tr [:cancel])
            :on-click on-cancel}]
          [uix/Button
           {:text     (@tr [:create])
            :primary  true
            :on-click #(on-submit @form-data)}]]]))))


(defn form-container-inner-modal-single
  [show? template on-submit on-cancel]
  (let [tr (subscribe [::i18n-subs/tr])
        form-data (reagent/atom nil)
        resourceMetadata (subscribe [::docs-subs/document template])]
    (fn [show? template on-submit on-cancel]
      (update-data form-data (:id template) nil nil)
      [ui/Modal {:open      show?
                 :onClose   on-cancel
                 :closeIcon true}
       [ui/ModalHeader (@tr [:create])]
       [ui/ModalContent {:scrolling true}
        (vec (concat [ui/Form]
                     (template-form form-data template @resourceMetadata)))]
       [ui/ModalActions
        [uix/Button
         {:text     (@tr [:cancel])
          :on-click on-cancel}]
        [uix/Button
         {:text     (@tr [:create])
          :primary  true
          :on-click #(on-submit @form-data)}]]])))
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
