(ns sixsq.slipstream.webui.deployment-detail.views
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
    [taoensso.timbre :as log]
    [reagent.core :as reagent]))


(defn ^:export set-runUUID [uuid]                           ;Used by old UI
  (dispatch [::deployment-detail-events/set-runUUID uuid]))


(defn nodes-list                                            ;FIXME
  []
  ["machine"])

(defn automatic-refresh
  [resource-id]
  (dispatch [::main-events/action-interval
             {:action    :start
              :id        :deployment-detail-get-deployment
              :frequency 30000
              :event     [::deployment-detail-events/get-deployment resource-id]}])
  (dispatch [::main-events/action-interval
             {:action    :start
              :id        :deployment-detail-get-summary-nodes-parameters
              :frequency 30000
              :event     [::deployment-detail-events/get-summary-nodes-parameters resource-id (nodes-list)]}])
  (dispatch [::main-events/action-interval
             {:action    :start
              :id        :deployment-detail-reports
              :frequency 30000
              :event     [::deployment-detail-events/get-reports resource-id]}])
  (dispatch [::main-events/action-interval
             {:action    :start
              :id        :deployment-detail-deployment-parameters
              :frequency 20000
              :event     [::deployment-detail-events/get-global-deployment-parameters resource-id]}])
  (dispatch [::main-events/action-interval
             {:action    :start
              :id        :deployment-detail-events
              :frequency 30000
              :event     [::deployment-detail-events/get-events resource-id]}]))


(def deployment-summary-keys #{:created
                               :updated
                               :resourceUri
                               :state
                               :id
                               :name
                               :description})


(defn module-name
  [deployment]
  (-> deployment :module :name))


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


(defn metadata-section
  []
  (let [deployment (subscribe [::deployment-detail-subs/deployment])]
    (fn []
      (let [summary-info (-> (select-keys @deployment deployment-summary-keys)
                             (merge (select-keys (:module @deployment) #{:name :path :type})))
            icon (-> @deployment :module :type deployment-detail-utils/category-icon)
            rows (map tuple-to-row summary-info)]
        [cc/metadata
         {:title       (module-name @deployment)
          :subtitle    (:state @deployment)
          :description (:startTime summary-info)
          :icon        icon}
         rows]))))


(defn node-parameter-table
  [params]
  [ui/Table style/definition
   (vec (concat [ui/TableBody] (map tuple-to-row params)))])


(defn parameter-to-row
  [{:keys [nodeID name description value] :as param}]
  [ui/Popup
   {:content (reagent/as-element [:p description])
    :trigger (reagent/as-element
               [ui/TableRow
                [ui/TableCell name]
                [ui/TableCell value]])}])

(defn global-parameters-section
  []
  (let [tr (subscribe [::i18n-subs/tr])
        deployment-parameters (subscribe [::deployment-detail-subs/global-deployment-parameters])]
    (fn []
      [cc/collapsible-segment (@tr [:global-parameters])
       [ui/Segment style/autoscroll-x
        [ui/Table style/single-line
         [ui/TableHeader
          [ui/TableRow
           [ui/TableHeaderCell [:span (@tr [:name])]]
           [ui/TableHeaderCell [:span (@tr [:value])]]]]
         (vec (concat [ui/TableBody]
                      (map parameter-to-row @deployment-parameters)))]]])))

(defn node-parameters-section
  []
  (let [tr (subscribe [::i18n-subs/tr])
        node-parameters (subscribe [::deployment-detail-subs/node-parameters])]
    (dispatch [::main-events/action-interval
               {:action    :start
                :id        :deployment-detail-get-node-parameters
                :frequency 5000
                :event     [::deployment-detail-events/get-node-parameters]}])
    (fn []
      [ui/Table style/single-line
       [ui/TableHeader
        [ui/TableRow
         [ui/TableHeaderCell [:span (@tr [:name])]]
         [ui/TableHeaderCell [:span (@tr [:value])]]]]
       (vec (concat [ui/TableBody]
                    (map parameter-to-row @node-parameters)))])))


(defn report-item
  [{:keys [id component created state] :as report}]
  ^{:key id} [:li
              (let [label (str/join " " [component created])]
                (if (= state "ready")
                  [:a {:style    {:cursor "pointer"}
                       :on-click #(dispatch [::deployment-detail-events/download-report id])} label]
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
        events (subscribe [::deployment-detail-subs/events])]
    (fn []
      (let [events (-> @events :events events-table-info)]
        [cc/collapsible-segment
         (@tr [:events])
         [events-table events]]))))

(defn reports-list-view
  []
  (let [reports (subscribe [::deployment-detail-subs/reports])]
    (if (seq @reports)
     (vec (concat [:ul] (mapv report-item (:externalObjects @reports))))
     [:p "Reports will be displayed as soon as available. No need to refresh."])))


(defn reports-list                                          ; Used by old UI
  []
  (let [runUUID (subscribe [::deployment-detail-subs/runUUID])]
    (when-not (str/blank? @runUUID)
      (dispatch [::main-events/action-interval
                 {:action    :start
                  :id        :deployment-detail-reports
                  :frequency 30000
                  :event     [::deployment-detail-events/get-reports @runUUID]}]))
    [reports-list-view]))


(defn reports-section
  [href]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn []
      [cc/collapsible-segment
       (@tr [:reports])
       [reports-list-view]])))


(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::deployment-detail-subs/loading?])
        deployment (subscribe [::deployment-detail-subs/deployment])]
    (fn []
      [uix/MenuItemWithIcon
       {:name      (@tr [:refresh])
        :icon-name "refresh"
        :loading?  @loading?
        :on-click  #(dispatch [::deployment-detail-events/get-deployment (:id @deployment)])}])))


(defn stop-button
  "Creates a button that will bring up a delete dialog and will execute the
   delete when confirmed."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        deployment (subscribe [::deployment-detail-subs/deployment])]
    (fn []
      (when-not (empty?
                  (filter #(= "http://schemas.dmtf.org/cimi/2/action/stop" (:rel %))
                          (get-in @deployment [:operations])))

        [resource-details/action-button-icon
         (@tr [:stop])
         "stop"
         (@tr [:stop])
         [:p (@tr [:are-you-sure?])]
         #(dispatch [::deployment-detail-events/stop-deployment (:id @deployment)])
         (constantly nil)]))))


(defn service-link-button
  []
  (let [deployment-parameters (subscribe [::deployment-detail-subs/global-deployment-parameters])]
    (fn []
      (let [link (:value (first (filter #(= "ss:url.service" (:name %)) @deployment-parameters)))]
        (when link
          [uix/MenuItemWithIcon
           {:name      (general/truncate link)
            :icon-name "external"
            :position  "right"
            :on-click  #(dispatch [::main-events/open-link link])}])))))


(defn node-card
  [node-name]
  (let [summary-nodes-parameters (subscribe [::deployment-detail-subs/summary-nodes-parameters])
        node-params (get @summary-nodes-parameters node-name [])
        params-by-name (into {} (map (juxt :name identity) node-params))
        service-url (get-in params-by-name ["url.service" :value])
        custom-state (get-in params-by-name ["statecustom" :value])
        ssh-url (get-in params-by-name ["url.ssh" :value])
        ssh-password (get-in params-by-name ["password.ssh" :value])
        complete (get-in params-by-name ["complete" :value])]
    ^{:key node-name}
    [ui/Card
     [ui/CardContent
      [ui/CardHeader [ui/Header node-name]]
      [ui/CardDescription
       [:div
        [ui/Icon {:name "external"}]
        [:a {:href service-url} service-url]]
       [:div
        [ui/Icon {:name "terminal"}]
        [:a {:href ssh-url} ssh-url]]
       [:div (str "password: " (when ssh-password "••••••"))
        (when ssh-password
          [ui/Popup {:trigger  (r/as-element [ui/CopyToClipboard {:text ssh-password}
                                              [:a [ui/Icon {:name "clipboard outline"}]]])
                     :position "top center"}
           "copy to clipboard"])]
       [:div custom-state]
       [:div [:a {:href     "#apache.1"
                  :on-click #(dispatch [::deployment-detail-events/show-node-parameters-modal node-name])}
              "details"]]
       (when (= complete "Ready")
         [:div {:align "right"} [ui/Icon {:name "check"}]])]]]))


(defn summary-section
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [cc/collapsible-segment
     (@tr [:summary])
     (vec (concat [ui/CardGroup {:centered true}]
                  (map node-card (nodes-list))))]))


(defn node-parameters-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        node-name (subscribe [::deployment-detail-subs/node-parameters-modal])
        deployment (subscribe [::deployment-detail-subs/deployment])]
    (fn []
      (let [hide-fn #(do (dispatch [::deployment-detail-events/close-node-parameters-modal])
                         (automatic-refresh (:id @deployment)))]
        [ui/Modal {:open       (boolean @node-name)
                   :close-icon true
                   :on-close   hide-fn}

         [ui/ModalHeader [ui/Icon {:name "microchip"}] @node-name]

         [ui/ModalContent {:scrolling true}
          [node-parameters-section]]

         [ui/ModalActions
          [uix/Button {:text     (@tr [:close]),
                       :on-click hide-fn}]]]))))


(defn menu
  []
  [ui/Menu {:borderless true}
   [refresh-button]
   [stop-button]
   [service-link-button]])


(defn deployment-detail
  [resource-id]
  (let [deployment (subscribe [::deployment-detail-subs/deployment])]
    (automatic-refresh resource-id)
    (fn [resource-id]
      [ui/Segment (merge style/basic
                         {:loading (not= resource-id (:id @deployment))})
       [ui/Container {:fluid true}
        [menu]
        [metadata-section]
        [summary-section]
        [global-parameters-section]
        [events-section]
        [reports-section resource-id]
        [node-parameters-modal]
        ]])))
