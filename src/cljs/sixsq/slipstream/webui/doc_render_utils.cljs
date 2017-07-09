(ns sixsq.slipstream.webui.doc-render-utils
  (:require
    [re-com.core :refer [h-box v-box box label title button modal-panel p]]
    [taoensso.timbre :as log]
    [reagent.core :as reagent]))

(defn edit-button
  "Creates an edit that will bring up an edit dialog and will save the
   modified resource when saved."
  [data action-fn]
  (let [show? (reagent/atom false)]
    (fn []
      [v-box
       :children [[button
                   :label "edit"
                   :class "btn-primary"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [v-box
                             :gap "2ex"
                             :size "auto"
                             :children [[:pre (with-out-str (cljs.pprint/pprint data))]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "save"
                                                 :class "btn btn-danger btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (action-fn)
                                                             (reset! show? false))]]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "cancel"
                                                 :class "btn btn-default btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (reset! show? false))]]]]])]])))

(defn delete-button
  "Creates a button that will bring up a delete dialog and will execute the
   delete when confirmed."
  [data action-fn]
  (let [show? (reagent/atom false)]
    (fn []
      [v-box
       :children [[button
                   :label "delete"
                   :class "btn-primary"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [v-box
                             :gap "2ex"
                             :width "auto"
                             :children [[title :level :level2 :label "Delete resource?"]
                                        [p "The resource identifier is " [:strong (:id data)] ". "
                                         "Delete operations " [:strong "cannot"] " be undone."]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "confirm"
                                                 :class "btn btn-danger btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (action-fn)
                                                             (reset! show? false))]]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "cancel"
                                                 :class "btn btn-default btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (reset! show? false))]]]]])]])))

(defn other-button
  "Creates a button that will bring up a dialog to confirm the given action."
  [label data action-fn]
  (let [show? (reagent/atom false)]
    (fn []
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
                             :children [[title :level :level2 :label (str "Execute action " label "?")]
                                        [p "Confirm executing action " [:strong label] " on " [:strong (:id data)] "."]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "confirm"
                                                 :class "btn btn-danger btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (action-fn)
                                                             (reset! show? false))]]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "cancel"
                                                 :class "btn btn-default btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (reset! show? false))]]]]])]])))
(defn operation-name [op-uri]
  (second (re-matches #"^(?:.*/)?(.+)$" op-uri)))

(defn operation-button [data [label href]]
  (case label
    "edit" [edit-button data #(js/alert "edit operation not implemented yet")]
    "delete" [delete-button data #(js/alert "delete operation not implemented yet")]
    [other-button label data #(js/alert "operation not implemented yet")]))

(defn format-operations [{:keys [operations] :as data} baseURI]
  (let [ops (map (juxt #(operation-name (:rel %)) #(str baseURI (:href %))) operations)]
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

(defn group-data-field [v]
  (fn []
    [box
     :align :start
     :child [label :label (or v "\u00a0")]]))

(defn group-kv-with-key [tag [k v]]
  (let [react-key (str "data-" tag "-" k)]
    ^{:key react-key} [group-data-field (str v)]))

(defn group-column-with-key [tag class-name column-data]
  ^{:key (str "column-" tag)}
  [v-box
   :class (str "webui-column " class-name)
   :children (vec (map (partial group-kv-with-key tag) column-data))])

(defn group-table
  [group-data]
  (let [value-column-data (sort-by first group-data)
        key-column-data (map (fn [[k _]] [k (strip-attr-ns k)]) value-column-data)]
    [h-box
     :class "webui-column-table"
     :children [[group-column-with-key "resource-keys" "webui-row-header" key-column-data]
                [group-column-with-key "resource-vals" "" value-column-data]]]))

(defn format-group [[group data]]
  ^{:key group}
  [v-box :children [[title
                     :label (str group)
                     :level :level2
                     :underline? true]
                    [group-table data]]])

(defn format-common [data]
  (let [common-data (select-keys data #{:name :description
                                        :id :resourceURI
                                        :created :updated})]
    (group-table common-data)))

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
