(ns sixsq.slipstream.webui.utils.resource-details
  (:require
    [cljs.pprint :refer [pprint]]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [cljsjs.codemirror.mode.javascript]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]
    [sixsq.slipstream.webui.cimi-detail.events :as cimi-detail-events]
    [sixsq.slipstream.webui.editor.editor :as editor]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.forms :as form-utils]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.table :as table]
    [sixsq.slipstream.webui.utils.time :as time]
    [sixsq.slipstream.webui.utils.ui-callback :as comp]
    [sixsq.slipstream.webui.utils.values :as values]))


(defn action-buttons
  [confirm-label cancel-label on-confirm on-cancel]
  [:div
   [uix/Button
    {:text     cancel-label
     :on-click on-cancel}]
   [uix/Button
    {:text     confirm-label
     :primary  true
     :on-click on-confirm}]])


(defn action-button-icon
  [label icon title-text body on-confirm on-cancel & [scrolling? position]]
  (let [show? (r/atom false)]
    (fn [label icon title-text body on-confirm on-cancel & [scrolling? position]]
      (let [action-fn (fn []
                        (reset! show? false)
                        (on-confirm))
            cancel-fn (fn []
                        (reset! show? false)
                        (on-cancel))]

        [ui/Modal
         {:open      (boolean @show?)
          :closeIcon true
          :on-close  #(reset! show? false)
          :trigger   (r/as-element
                       [ui/MenuItem (cond-> {:aria-label label, :name label, :on-click #(reset! show? true)}
                                            position (assoc :position position))
                        (when icon
                          [ui/Icon {:name icon}])
                        label])}
         [ui/ModalHeader title-text]
         [ui/ModalContent (cond-> {}
                                  scrolling? (assoc :scrolling true)) body]
         [ui/ModalActions
          [action-buttons
           label
           "cancel"
           action-fn
           cancel-fn]]]))))


(defn action-button
  [label title-text body on-confirm on-cancel & [scrolling?]]
  [action-button-icon label nil title-text body on-confirm on-cancel scrolling?])


(defn update-data [text form-id param value]
  (let [edn-text (general/json->edn @text)
        data (cond-> edn-text
                     param (merge {param value}))
        result (merge edn-text data)]
    (reset! text (general/edn->json result))))


(defn template-form
  [text {:keys [template-resource-key params-desc] :as description}]
  (let [default-data (->> (general/json->edn @text) (map (fn [[k v]] [k {:data v}])) (into {}))
        params-desc-with-data (-> (merge-with merge params-desc default-data)
                                  (dissoc :acl)
                                  (dissoc :operations)
                                  (dissoc template-resource-key))
        description-with-data (assoc description :params-desc params-desc-with-data)
        [hidden-params visible-params] (form-utils/ordered-params description-with-data)
        update-data-fn (partial update-data text)
        form-component-fn (partial ff/form-field update-data-fn nil)]
    (mapv form-component-fn (concat hidden-params visible-params))))


(defn edit-button
  "Creates an edit that will bring up an edit dialog and will save the
   modified resource when saved."
  [data description action-fn]
  (let [tr (subscribe [::i18n-subs/tr])
        text (r/atom (general/edn->json data))
        editor-mode? (r/atom false)
        json-error? (r/atom false)]
    (fn [data description action-fn]
      [action-button-icon
       (@tr [:update])
       "pencil"
       (str (@tr [:editing]) " " (:id data))
       (if description
         [:div
          [ui/Menu {:attached "top"}
           [ui/MenuItem {:icon    (if @editor-mode? "list layout" "code")
                         :active  @editor-mode?
                         :onClick (comp/callback :active (fn [active-v]
                                                           (reset! json-error? false)
                                                           (try
                                                             (general/json->edn @text)
                                                             (reset! json-error? false)
                                                             (reset! editor-mode? (not active-v))
                                                             (catch js/Object e
                                                               (reset! json-error? true)
                                                               (reset! editor-mode? true)))))}]
           (when @json-error?
             [ui/MenuItem [ui/Label "Invalid JSON!!!"]])]
          [ui/Segment {:attached "bottom"}
           (if @editor-mode?
             [editor/json-editor text]
             (vec (concat [ui/Form]
                          (template-form text description))))]]
         [editor/json-editor text])
       (fn []
         (try
           (action-fn (general/json->edn @text))
           (catch :default e
             (action-fn e))))
       (constantly nil)
       true])))


(defn delete-button
  "Creates a button that will bring up a delete dialog and will execute the
   delete when confirmed."
  [data action-fn]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [data action-fn]
      [action-button-icon
       (@tr [:delete])
       "trash"
       (@tr [:delete-resource])
       [:p (@tr [:delete-resource-msg] [(:id data)])]
       action-fn
       (constantly nil)])))


(defn other-button
  "Creates a button that will bring up a dialog to confirm the given action."
  [label data action-fn]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [label data action-fn]
      [action-button-icon
       label
       (case label
         "download" "cloud download"
         "upload" "cloud upload"
         "describe" "info"
         "ready" "check"
         "start" "play"
         "stop" "stop"
         "collect" "cog"
         nil)
       (@tr [:execute-action] [label])
       [:p (@tr [:execute-action-msg] [label (:id data)])]
       action-fn
       (constantly nil)])))


(defn operation-name [op-uri]
  (second (re-matches #"^(?:.*/)?(.+)$" op-uri)))


;; Explicit keys have been added to the operation buttons to avoid react
;; errors for duplicate keys, which may happen when the data contains :key.
;; It is probably a bad idea to have a first argument that can be a map
;; as this will be confused with reagent options.
(defn operation-button [{:keys [id] :as data} description [label href operation-uri]]
  (case label
    "edit" ^{:key "edit"} [edit-button data description #(dispatch [::cimi-detail-events/edit id %])]
    "delete" ^{:key "delete"} [delete-button data #(dispatch [::cimi-detail-events/delete id])]
    ^{:key operation-uri} [other-button label data #(dispatch [::cimi-detail-events/operation id operation-uri])]))


(defn format-operations [refresh-button {:keys [operations] :as data} baseURI description]
  (let [ops (map (juxt #(operation-name (:rel %)) #(str baseURI (:href %)) :rel) operations)]
    (vec (concat [refresh-button] (map (partial operation-button data description) ops)))))


(defn metadata-row
  [k v]
  (let [value (cond
                (vector? v) v
                (map? v) (with-out-str (pprint v))
                :else (str v))]
    [ui/TableRow
     [ui/TableCell {:collapsing true} (str k)]
     [ui/TableCell value]]))


(defn detail-header
  [{:keys [id resourceURI created updated
           name description properties acl] :as data}]
  (when data
    [cc/metadata
     {:title       (or name id)
      :subtitle    (when name id)
      :description description
      :icon        "file"
      :updated     updated
      :acl         acl
      :properties  properties}
     (cond-> []
             id (conj (metadata-row "id" id))
             resourceURI (conj (metadata-row "resourceURI" resourceURI))
             name (conj (metadata-row "name" name))
             description (conj (metadata-row "description" description))
             created (conj (metadata-row "created" (time/time-value created)))
             updated (conj (metadata-row "updated" (time/time-value updated))))]))


(defn strip-attr-ns
  "Strips the attribute namespace from the given key."
  [k]
  (last (re-matches #"(?:([^:]*):)?(.*)" (name k))))


(defn tuple-to-row
  [[key value display-name helper]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (or display-name (str key)) ff/nbsp [ff/help-popup helper]]
   [ui/TableCell {:style {:max-width     "80ex"             ;; FIXME: need to get this from parent container
                          :text-overflow "ellipsis"
                          :overflow      "hidden"}} (if (vector? value)
                                                      (values/format-collection value)
                                                      (values/format-value value))]])


(defn data-to-tuple-fn
  [params-desc]
  (juxt
    (comp strip-attr-ns first)
    second
    #(get-in params-desc [(keyword (first %)) :displayName])
    #(get-in params-desc [(keyword (first %)) :description])))


(defn group-table-sui
  [group-data {:keys [params-desc] :as description}]
  (let [data (sort-by first group-data)]
    (table/definition-table (->> data
                                 (map (data-to-tuple-fn params-desc))
                                 (map tuple-to-row)))))


(defn detail-menu
  [refresh-button data baseURI description]
  (vec (concat [ui/Menu {:borderless true}]
               (format-operations refresh-button data baseURI description))))


(defn resource-detail
  "Provides a generic visualization of a CIMI resource document."
  [refresh-button data baseURI description]
  (vec (concat
         [ui/Segment style/basic
          (detail-menu refresh-button data baseURI description)
          (detail-header data)
          (group-table-sui (cimi-api-utils/remove-common-attrs data) description)])))
