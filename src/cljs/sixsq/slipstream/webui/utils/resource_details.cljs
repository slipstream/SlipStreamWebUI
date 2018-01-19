(ns sixsq.slipstream.webui.utils.resource-details
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]

    [cljs.pprint :refer [pprint]]

    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [cljsjs.codemirror.mode.javascript]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.editor.editor :as editor]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]

    [taoensso.timbre :as log]))


(defn action-buttons
  [confirm-label cancel-label on-confirm on-cancel]
  [:div
   [ui/Button
    {:on-click on-cancel}
    cancel-label]
   [ui/Button
    {:on-click on-confirm}
    confirm-label]])


(defn action-button
  [label title-text body on-confirm on-cancel & [scrolling?]]
  (let [show? (reagent/atom false)]
    (fn [label title-text body on-confirm on-cancel & [scrolling?]]
      (let [action-fn (fn []
                        (reset! show? false)
                        (on-confirm))
            cancel-fn (fn []
                        (reset! show? false)
                        (on-cancel))]

        [ui/Button
         {:circular false
          :primary  true
          :on-click #(reset! show? true)}
         label
         [ui/Modal
          (cond-> {:open     (boolean @show?)
                   :on-close #(reset! show? false)}
                  scrolling? (assoc :scrolling true))
          [ui/ModalHeader title-text]
          [ui/ModalContent
           [:p body]]
          [ui/ModalActions
           [action-buttons
            label
            "cancel"
            action-fn
            cancel-fn]]]]))))


(defn edit-button
  "Creates an edit that will bring up an edit dialog and will save the
   modified resource when saved."
  [data action-fn]
  (let [tr (subscribe [::i18n-subs/tr])
        text (reagent/atom "")]
    (fn [data action-fn]
      (reset! text (.stringify js/JSON (clj->js data) nil 2))
      [action-button
       (@tr [:update])
       (str (@tr [:editing]) " " (:id data))
       [editor/json-editor
        :text text
        :on-change #(reset! text %)]
       (fn []
         (try
           (let [data (js->clj (.parse js/JSON @text))]
             (action-fn data))
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


(defn operation-button [data [label href operation-uri]]
  (case label
    "edit" [edit-button data #(dispatch [:evt.webui.cimi/edit (:id data) %])]
    "delete" [delete-button data #(dispatch [:evt.webui.cimi/delete (:id data)])]
    [other-button label data #(dispatch [:evt.webui.cimi/operation (:id data) operation-uri])]))


(defn format-operations [refresh-button {:keys [operations] :as data} baseURI]
  (let [ops (map (juxt #(operation-name (:rel %)) #(str baseURI (:href %)) :rel) operations)]
    (vec (concat [:div refresh-button] (map (partial operation-button data) ops)))))


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


(defn tuple-to-row [[v1 v2]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (str v1)]
   [ui/TableCell {:style {:max-width     "80ex"             ;; FIXME: need to get this from parent container
                          :text-overflow "ellipsis"
                          :overflow      "hidden"}} (str v2)]])


(defn group-table-sui
  [group-data]
  (let [data (sort-by first group-data)]
    [ui/Table {:compact     true
               :definition  true
               :single-line true
               :padded      false
               :style       {:max-width "100%"}}
     (vec (concat [ui/TableBody]
                  (map tuple-to-row (map (juxt (comp strip-attr-ns first) second) data))))]))


(defn format-group [[group data]]
  ^{:key group}
  [ui/Card {:fluid true}
   [ui/CardContent
    [ui/CardHeader (str group)]
    [ui/CardDescription
     [group-table-sui data]]]])


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


(defn format-resource-data [data]
  (let [groups (into (sorted-map-by group-comparator)
                     (group-by attr-ns (-> data
                                           reformat-acl
                                           (dissoc :operations))))]
    (doall (map format-group groups))))


(defn resource-detail
  "Provides a generic visualization of a CIMI resource document."
  [refresh-button title {:keys [name id operations] :as data} baseURI]
  [:div
   [:h1 (or name id title)]
   (format-operations refresh-button data baseURI)
   (if-not (instance? js/Error data)
     (format-resource-data data)
     [:pre (with-out-str (pprint (ex-data data)))])])
