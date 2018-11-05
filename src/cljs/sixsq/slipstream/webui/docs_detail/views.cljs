(ns sixsq.slipstream.webui.docs-detail.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [sixsq.slipstream.webui.deployment-detail.events :as deployment-detail-events]
    [sixsq.slipstream.webui.deployment-detail.subs :as deployment-detail-subs]
    [sixsq.slipstream.webui.deployment-detail.utils :as deployment-detail-utils]
    [sixsq.slipstream.webui.history.views :as history]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.resource-details :as resource-details]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]))


(def summary-keys #{:creation
                    :category
                    :resourceUri
                    :lastStateChangeTime
                    :startTime
                    :endTime
                    :state
                    :type
                    :user
                    :cloudServiceNames})


(def terminate-summary-keys #{:resourceUri
                              :startTime
                              :state
                              :user
                              :cloudServiceNames})


(defn module-name
  [resource]
  (-> resource :run :module :name))


(defn format-parameter-key
  [k]
  (let [key-as-string (name k)]
    (if-let [abbrev-name (second (re-matches #"^.*:(.*)$" key-as-string))]
      abbrev-name
      key-as-string)))


(defn format-module-link
  [module]
  [history/link (str "application/" module) module])


(defn format-parameter-value
  [k v]
  (let [value (str v)]
    (cond
      (re-matches #"^.*:url.*$" (name k)) [:a {:href value} value]
      (= :module k) (format-module-link value)
      :else value)))


(defn tuple-to-row
  [[key value]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (format-parameter-key key)]
   [ui/TableCell {:style {:max-width     "80ex"             ;; FIXME: need to get this from parent container
                          :text-overflow "ellipsis"
                          :overflow      "hidden"}} (format-parameter-value key value)]])


(defn category-icon
  [category]
  (case category
    "Project" "folder"
    "Deployment" "sitemap"
    "Image" "microchip"
    "question circle"))


(defn metadata-section
  [{:keys [id name description] :as document}]
  [cc/metadata
   {:title       name
    :subtitle    id
    :description description
    :icon        "book"}])


(defn terminate-summary
  []
  (let [resource (subscribe [::deployment-detail-subs/resource])]
    (fn []
      (let [module (module-name @resource)
            summary-info (-> (select-keys (:run @resource) terminate-summary-keys)
                             (assoc :module module))]
        [ui/Table style/definition
         (vec (concat [ui/TableBody]
                      (map tuple-to-row summary-info)))]))))


(def node-parameter-pattern #"([^:]+?)(\.\d+)?:(.+)")


(defn parameter-section
  [[k _]]
  (when-let [[_ node] (re-matches node-parameter-pattern k)]
    node))


(defn node-parameter-table
  [params]
  [ui/Table style/definition
   (vec (concat [ui/TableBody] (map tuple-to-row params)))])


(defn grouped-parameters
  [resource]
  (let [parameters (-> resource :run :runtimeParameters :entry)
        parameters-kv (->> parameters
                           (mapv (juxt :string #(-> % :runtimeParameter :content)))
                           (group-by parameter-section))]
    parameters-kv))


(defn parameters-dropdown
  [selected-section]
  (let [resource (subscribe [::deployment-detail-subs/resource])]
    (let [parameter-groups (sort (keys (grouped-parameters @resource)))]
      [ui/Dropdown
       {:options       (map #(identity {:key %, :text %, :value %}) parameter-groups)
        :selection     true
        :default-value "ss"
        :on-change     (ui-callback/value #(reset! selected-section %))}])))

(defn row-attribute-fn [{:keys [name description] :as entry}]
  [ui/TableRow
   [ui/TableCell {:collapsing true} name]
   [ui/TableCell {:style {:max-width     "150px"
                          :overflow      "hidden"
                          :text-overflow "ellipsis"}} description]])


(defn attributes-table
  [document]
  (let [tr (subscribe [::i18n-subs/tr])]
    (log/error document)
    [ui/Segment (merge style/basic
                       {:class-name "webui-x-autoscroll"})

     [ui/Table
      {:compact     "very"
       :single-line true
       :padded      false
       :unstackable true
       :selectable  true}
      [ui/TableHeader
       [ui/TableRow
        [ui/TableHeaderCell (@tr [:name])]
        [ui/TableHeaderCell (@tr [:description])]]]
      (vec (concat [ui/TableBody]
                   (map row-attribute-fn (sort-by :name (get document :attributes)))))]]))

(defn attributes-section
  [document]
  (let [tr (subscribe [::i18n-subs/tr])
        resource (subscribe [::deployment-detail-subs/resource])
        selected-section (r/atom nil)]
    (fn []
      (let [
            ;;parameters-kv (grouped-parameters @resource)
            ;;parameter-group (get parameters-kv (or @selected-section "ss"))
            ;;parameter-table (node-parameter-table parameter-group)
            ;;contents (vec (concat [:div] [[parameters-dropdown selected-section]] [parameter-table]))
            ]
        [cc/collapsible-segment (@tr [:attributes])
         [attributes-table document]]))))


(defn report-item
  [{:keys [id component created state] :as report}]
  ^{:key id} [:li
              (let [label (str/join " " [component created])]
                (if (= state "ready")
                  [:a {:onClick #(dispatch [::deployment-detail-events/download-report id])} label]
                  label))])


(def event-fields #{:id :content :timestamp :type})


(defn events-table-info
  [events]
  (when-let [start (-> events last :timestamp)]
    (let [dt-fn (partial deployment-detail-utils/assoc-delta-time start)]
      (->> events
           (map #(select-keys % event-fields))
           (map dt-fn)))))


(defn format-event-id
  [id]
  (let [tag (second (re-matches #"^.*/([^-]+).*$" id))]
    [history/link (str "cimi/" id) tag]))


(defn event-map-to-row
  [{:keys [id content timestamp type delta-time] :as evt}]
  [ui/TableRow
   [ui/TableCell (format-event-id id)]
   [ui/TableCell timestamp]
   [ui/TableCell delta-time]
   [ui/TableCell type]
   [ui/TableCell (:state content)]])


(defn events-table
  [events]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [events]
      [ui/Segment style/autoscroll-x
       [ui/Table style/single-line
        [ui/TableHeader
         [ui/TableRow
          [ui/TableHeaderCell [:span (@tr [:event])]]
          [ui/TableHeaderCell [:span (@tr [:timestamp])]]
          [ui/TableHeaderCell [:span (@tr [:delta-min])]]
          [ui/TableHeaderCell [:span (@tr [:type])]]
          [ui/TableHeaderCell [:span (@tr [:state])]]]]
        (vec (concat [ui/TableBody]
                     (map event-map-to-row events)))]])))


(defn events-section
  []
  (let [tr (subscribe [::i18n-subs/tr])
        events-collection (subscribe [::deployment-detail-subs/events])]
    (dispatch [::deployment-detail-events/fetch-events])
    (dispatch [::main-events/action-interval
               {:action    :start
                :id        :deployment-detail-events
                :frequency 30000
                :event     [::deployment-detail-events/fetch-events]}])
    (fn []
      (let [events (-> @events-collection :events events-table-info)]
        [cc/collapsible-segment
         (@tr [:events])
         [events-table events]]))))


(defn reports-list
  []
  (let [reports (subscribe [::deployment-detail-subs/reports])
        runUUID (subscribe [::deployment-detail-subs/runUUID])]
    (when-not (str/blank? @runUUID)
      (dispatch [::main-events/action-interval
                 {:action    :start
                  :id        :deployment-detail-reports
                  :frequency 30000
                  :event     [::deployment-detail-events/fetch-reports]}]))
    (if (seq @reports)
      (vec (concat [:ul] (mapv report-item (:externalObjects @reports))))
      [:p "Reports will be displayed as soon as available. No need to refresh."])))


(defn reports-section
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn []
      [cc/collapsible-segment
       (@tr [:reports])
       [reports-list]])))


(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::deployment-detail-subs/loading?])
        runUUID (subscribe [::deployment-detail-subs/runUUID])]
    (fn []
      [uix/MenuItemWithIcon
       {:name      (@tr [:refresh])
        :icon-name "refresh"
        :loading?  @loading?
        :on-click  #(dispatch [::deployment-detail-events/get-deployment @runUUID])}])))


;; FIXME: Remove duplicated function.
(defn is-terminated-state? [state]
  (#{"Finalizing" "Done" "Aborted" "Cancelled"} state))


(defn terminate-button
  "Creates a button that will bring up a delete dialog and will execute the
   delete when confirmed."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        cached-resource-id (subscribe [::deployment-detail-subs/cached-resource-id])
        resource (subscribe [::deployment-detail-subs/resource])]
    (fn []
      (when-let [state (-> @resource :run :state)]
        (when-not (is-terminated-state? state)
          [resource-details/action-button-icon
           (@tr [:terminate])
           "close"
           (@tr [:terminate])
           [terminate-summary]
           #(dispatch [::deployment-detail-events/terminate-deployment @cached-resource-id])
           (constantly nil)])))))


(defn service-link-button
  []
  (let [resource (subscribe [::deployment-detail-subs/resource])]
    (fn []
      (let [parameters-kv (grouped-parameters @resource)
            global-params (get parameters-kv "ss")
            state (second (first (filter #(= "ss:state" (first %)) global-params)))
            link (second (first (filter #(= "ss:url.service" (first %)) global-params)))]
        (when (and link (= "Ready" state))
          [uix/MenuItemWithIcon
           {:name      (general/truncate link)
            :icon-name "external"
            :position  "right"
            :on-click  #(dispatch [::deployment-detail-events/open-link link])}])))))


(defn menu
  []
  [ui/Menu {:borderless true}
   [refresh-button]
   [terminate-button]
   [service-link-button]])


(defn docs-detail
  [document]
  (let [runUUID (subscribe [::deployment-detail-subs/runUUID])
        cached-resource-id (subscribe [::deployment-detail-subs/cached-resource-id])
        resource (subscribe [::deployment-detail-subs/resource])]
    (fn [document]
      #_(pr-str document)
      [ui/Container {:fluid   true}
       [metadata-section document]
       [attributes-section document]
       #_[events-section]
       #_[reports-section]])))
