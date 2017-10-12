(ns sixsq.slipstream.webui.doc-render-utils
  (:require
    [re-com.core :refer [h-box v-box box label title button modal-panel p scroller gap]]
    [re-frame.core :refer [dispatch]]
    [taoensso.timbre :as log]
    [reagent.core :as reagent]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [cljsjs.codemirror.mode.javascript]
    [sixsq.slipstream.webui.components.editor :as editor]
    [sixsq.slipstream.webui.components.core :refer [column]]))

(defn action-buttons
  [confirm-label cancel-label on-confirm on-cancel]
  [h-box
   :justify :between
   :children [[button
               :label cancel-label
               :class "btn btn-default"
               :disabled? false
               :on-click on-cancel]
              [button
               :label confirm-label
               :class "btn btn-primary"
               :disabled? false
               :on-click on-confirm]]])

(defn action-button
  [label title-text body on-confirm on-cancel]
  (let [show? (reagent/atom false)]
    (fn [label title-text body on-confirm on-cancel]
      (let [action-fn (fn []
                        (reset! show? false)
                        (on-confirm))
            cancel-fn (fn []
                        (reset! show? false)
                        (on-cancel))]
        [v-box
         :children [[button
                     :label label
                     :class "btn-primary"
                     :on-click #(reset! show? true)]
                    (when @show?
                      [modal-panel
                       :backdrop-on-click #(reset! show? false)
                       :child [v-box
                               :gap "2ex"
                               :width "auto"
                               :children [[title
                                           :level :level2
                                           :label title-text]
                                          body
                                          [action-buttons
                                           label
                                           "cancel"
                                           action-fn
                                           cancel-fn]]]])]]))))


(defn edit-button
  "Creates an edit that will bring up an edit dialog and will save the
   modified resource when saved."
  [data action-fn]
  (let [text (reagent/atom "")]
    (fn [data action-fn]
      (reset! text (.stringify js/JSON (clj->js data) nil 2))
      [action-button
       "edit"
       (str "Editing " (:id data))
       [scroller
        :min-width "80ex"
        :min-height "10em"
        :child [v-box
                :gap "1ex"
                :children [[editor/json-editor
                            :text text
                            :on-change #(reset! text %)]]]]
       (fn []
         (try
           (let [data (js->clj (.parse js/JSON @text))]
             (action-fn data))
           (catch js/Error e
             (action-fn e))))
       (constantly nil)])))


(defn delete-button
  "Creates a button that will bring up a delete dialog and will execute the
   delete when confirmed."
  [data action-fn]
  [action-button
   "delete"
   "Delete resource?"
   [p "The resource identifier is " [:strong (:id data)] ". "
    "Delete operations " [:strong "cannot"] " be undone."]
   action-fn
   (constantly nil)])

(defn other-button
  "Creates a button that will bring up a dialog to confirm the given action."
  [label data action-fn]
  [action-button
   label
   (str "Execute action " label "?")
   [p "Confirm executing action " [:strong label] " on " [:strong (:id data)] "."]
   action-fn
   (constantly nil)])

(defn operation-name [op-uri]
  (second (re-matches #"^(?:.*/)?(.+)$" op-uri)))

(defn operation-button [data [label href operation-uri]]
  (case label
    "edit" [edit-button data #(dispatch [:evt.webui.cimi/edit (:id data) %])]
    "delete" [delete-button data #(dispatch [:evt.webui.cimi/delete (:id data)])]
    [other-button label data #(dispatch [:evt.webui.cimi/operation (:id data) operation-uri])]))

(defn format-operations [{:keys [operations] :as data} baseURI]
  (let [ops (map (juxt #(operation-name (:rel %)) #(str baseURI (:href %)) :rel) operations)]
    [h-box
     :gap "1ex"
     :children (map (partial operation-button data) ops)]))

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

(defn group-table
  [group-data]
  (let [data (sort-by first group-data)]
    [h-box
     :class "webui-column-table"
     :gap "1ex"
     :children [[column
                 :model data
                 :key-fn first
                 :value-fn (comp strip-attr-ns first)
                 :value-class "webui-row-header"]
                [column
                 :model data
                 :key-fn first
                 :value-fn second]]]))

(defn format-group [[group data]]
  ^{:key group}
  [v-box :children [[title
                     :label (str group)
                     :level :level2
                     :underline? true]
                    [group-table data]]])

(defn reformat-acl [{{:keys [owner rules] :as acl} :acl :as data}]
  (let [own ["acl:owner" (str (:principal owner) " (" (:type owner) ")")]
        rul (map (fn [{:keys [principal type right]} i]
                   [(str "acl:rule-" i)
                    (str principal " (" type ") = " right)]) rules (range))
        entries (concat [own] rul)]
    (into {} (concat (seq (dissoc data :acl))
                     entries))))

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

(defn format-resource-data [data]
  (let [groups (into (sorted-map-by group-comparator)
                     (group-by attr-ns (-> data
                                           reformat-acl
                                           (dissoc :operations))))]
    (doall (map format-group groups))))

(defn resource-detail
  "Provides a generic visualization of a CIMI resource document."
  [{:keys [name id operations] :as data} baseURI]
  (when data
    [v-box
     :children [[title
                 :label (or name id)
                 :level :level1
                 :underline? true]
                (when operations
                  (format-operations data baseURI))
                (format-resource-data data)]]))
