(ns sixsq.slipstream.webui.utils.resource-details
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]

    [cljs.pprint :refer [pprint]]

    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [cljsjs.codemirror.mode.javascript]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.editor.editor :as editor]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]

    [sixsq.slipstream.webui.cimi-detail.events :as cimi-detail-events]

    [sixsq.slipstream.webui.utils.form-fields :as ff]
    [sixsq.slipstream.webui.utils.component :as comp]

    [sixsq.slipstream.webui.utils.forms :as form-utils]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.values :as values]))


(defn action-buttons
  [confirm-label cancel-label on-confirm on-cancel]
  [:div
   [ui/Button
    {:on-click on-cancel}
    cancel-label]
   [ui/Button
    {:primary  true
     :on-click on-confirm}
    confirm-label]])


(defn action-button
  [label title-text body on-confirm on-cancel & [scrolling?]]
  (let [show? (r/atom false)]
    (fn [label title-text body on-confirm on-cancel & [scrolling?]]
      (let [action-fn (fn []
                        (reset! show? false)
                        (on-confirm))
            cancel-fn (fn []
                        (reset! show? false)
                        (on-cancel))]

        [ui/Modal
         (cond-> {:open      (boolean @show?)
                  :closeIcon true
                  :on-close  #(reset! show? false)
                  :trigger   (r/as-element [ui/Button
                                            {:primary  true
                                             :on-click #(reset! show? true)}
                                            label])}
                 scrolling? (assoc :scrolling true))
         [ui/ModalHeader title-text]
         [ui/ModalContent body]
         [ui/ModalActions
          [action-buttons
           label
           "cancel"
           action-fn
           cancel-fn]]]))))


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
    (vec (map form-component-fn (concat hidden-params visible-params)))))


(defn edit-button
  "Creates an edit that will bring up an edit dialog and will save the
   modified resource when saved."
  [data description action-fn]
  (let [tr (subscribe [::i18n-subs/tr])
        text (r/atom (general/edn->json data))
        editor-mode? (r/atom false)
        json-error? (r/atom false)]
    (fn [data description action-fn]
      [action-button
       (@tr [:update])
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
                                                               (reset! editor-mode? true)))
                                                           ))}]
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
           (catch js/Error e
             (action-fn e))))
       (constantly nil)
       true])))


(defn delete-button
  "Creates a button that will bring up a delete dialog and will execute the
   delete when confirmed."
  [data action-fn]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [data action-fn]
      [action-button
       (@tr [:delete])
       (@tr [:delete-resource])
       [:p (@tr [:delete-resource-msg] [(:id data)])]
       action-fn
       (constantly nil)])))


(defn other-button
  "Creates a button that will bring up a dialog to confirm the given action."
  [label data action-fn]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [label data action-fn]
      [action-button
       label
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
    ^{:key other-button} [other-button label data #(dispatch [::cimi-detail-events/operation id operation-uri])]))


(defn format-operations [refresh-button {:keys [operations] :as data} baseURI description]
  (let [ops (map (juxt #(operation-name (:rel %)) #(str baseURI (:href %)) :rel) operations)]
    [ui/Card {:fluid true}
     [ui/CardContent
      (vec (concat [ui/CardDescription refresh-button] (map (partial operation-button data description) ops)))]]))


(defn attr-ns
  "Extracts the attribute namespace for the given key-value pair.
   Returns 'attributes' if there is no explicit namespace."
  [[k _]]
  (let [prefix (second (re-matches #"(?:([^:]*):)?(.*)" (name k)))]
    (cond
      prefix prefix
      (#{:id :resourceURI :created :updated :name :description} k) "common"
      (= k :acl) "acl"
      (= k :operations) "operations"
      (= k :properties) "properties"
      :else "attributes")))


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
                          :overflow      "hidden"}} (values/format-value value)]])


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
    [ui/Table {:compact     true
               :definition  true
               :single-line true
               :padded      false
               :style       {:max-width "100%"}}
     (vec (concat [ui/TableBody]
                  (->> data
                       (map (data-to-tuple-fn params-desc))
                       (map tuple-to-row))))]))


(defn format-group [description [group data]]
  ^{:key group}
  [ui/Card {:fluid true}
   [ui/CardContent
    [ui/CardHeader (str group)]
    [ui/CardDescription
     [group-table-sui data description]]]])


(defn reformat-acl [{{:keys [owner rules] :as acl} :acl :as data}]
  (let [own ["acl:owner" (str (:principal owner) " (" (:type owner) ")")]
        rul (map (fn [{:keys [principal type right]} i]
                   [(str "acl:rule-" i)
                    (str principal " (" type ") = " right)]) rules (range))
        entries (concat [own] rul)]
    (into {} (concat (seq (dissoc data :acl)) entries))))


(defn group-comparator [x y]
  (let [group-order (zipmap ["common" "properties" "attributes" :others "operations" "acl"] (range))
        x-index (get group-order x)
        y-index (get group-order y)
        o-index (get group-order :others)]
    (cond
      (and x-index y-index) (< x-index y-index)
      (and (nil? x-index) (nil? y-index)) (neg? (compare x y))
      y-index (< o-index y-index)
      x-index (< x-index o-index))))


(defn format-resource-data [data description]
  (let [groups (into (sorted-map-by group-comparator)
                     (group-by attr-ns (-> data
                                           reformat-acl
                                           (dissoc :operations))))]
    (doall (map (partial format-group description) groups))))


(defn resource-detail
  "Provides a generic visualization of a CIMI resource document."
  [refresh-button title {:keys [name id operations] :as data} baseURI description]
  [:div
   [:h1 (or name id title)]
   (format-operations refresh-button data baseURI description)
   (format-resource-data data description)])
