(ns sixsq.slipstream.webui.deployment-detail.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [reagent.core :as reagent]
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
    [sixsq.slipstream.webui.utils.time :as time]))


(defn ^:export set-runUUID
  [uuid]                                                    ;Used by old UI
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
  (let [deployment (subscribe [::deployment-detail-subs/deployment])
        deployment-parameters (subscribe [::deployment-detail-subs/global-deployment-parameters])]
    (fn []
      (let [summary-info (-> (select-keys @deployment deployment-summary-keys)
                             (merge (select-keys (:module @deployment) #{:name :path :type})))
            icon (-> @deployment :module :type deployment-detail-utils/category-icon)
            rows (map tuple-to-row summary-info)
            ss-state (-> @deployment-parameters (get "ss:state" {}) :value)]
        [cc/metadata
         (cond-> {:title       (module-name @deployment)
                  :description (:startTime summary-info)
                  :icon        icon}
                 ss-state (assoc :subtitle ss-state))
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
      (let [global-params (vals @deployment-parameters)]
        [cc/collapsible-segment (@tr [:global-parameters])
         [ui/Segment style/autoscroll-x
          [ui/Table style/single-line
           [ui/TableHeader
            [ui/TableRow
             [ui/TableHeaderCell [:span (@tr [:name])]]
             [ui/TableHeaderCell [:span (@tr [:value])]]]]
           (when-not (empty? global-params)
             (vec (concat [ui/TableBody]
                          (map parameter-to-row global-params))))]]]))))

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
      (let [events (events-table-info @events)]
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
      (let [link (-> @deployment-parameters (get "ss:url.service") :value)]
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
       (when service-url
         [:div
          [ui/Icon {:name "external"}]
          [:a {:href service-url, :target "_blank"} service-url]])
       (when ssh-url
         [:div
          [ui/Icon {:name "terminal"}]
          [:a {:href ssh-url} ssh-url]])
       (when ssh-password
         [:div (str "password: " (when ssh-password "••••••"))
          [ui/Popup {:trigger  (r/as-element [ui/CopyToClipboard {:text ssh-password}
                                              [:a [ui/Icon {:name "clipboard outline"}]]])
                     :position "top center"}
           "copy to clipboard"]])
       (when custom-state
         [:div custom-state])
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

(def deployment-states ["Provisioning" "Executing" "SendingReports" "Ready" "Done"])

(def states-map (into {} (map-indexed (fn [i state] [state i]) deployment-states)))

(def steps [["Provisioning" "Provisioning" "Starting system"]
            ["Executing" "Executing" "Executing recipies"]
            ["Reporting" "SendingReports" "Gathering for posterity"]
            ["Ready" "Ready" "All systems go ready"]])

(defn event-get-timestamp
  [event]
  (-> event :timestamp time/parse-iso8601))

(defn step-items
  [locale active-state-index events-map [title state description]]
  (let [event-state (get events-map state)
        state-index (get states-map state)
        is-state-active? (= active-state-index state-index)
        is-state-completed? (> active-state-index state-index)]
    {:key         state
     :title       title
     :description (str description
                       (cond
                         is-state-active? (str ". Running for " (time/delta-humanize
                                                                  (event-get-timestamp event-state)
                                                                  locale))
                         is-state-completed? (str ". Took " (time/delta-humanize
                                                              (event-get-timestamp event-state)
                                                              (some->> (inc state-index)
                                                                       (nth deployment-states)
                                                                       (get events-map)
                                                                       event-get-timestamp)
                                                              locale))))
     :icon        (cond
                    is-state-completed? "check"
                    is-state-active? "rocket"
                    :else "ellipsis horizontal")
     :active      is-state-active?
     :disabled    (< active-state-index state-index)}))

(defn extract-steps
  [events]
  (let [locale (subscribe [::i18n-subs/locale])
        events-dev [
                    {:severity "medium", :id "event/7ebb6b3b-cc7a-45db-a4ba-adb01dc2e6a3", :type "state", :content {:resource {:href "deployment/55976d06-bfff-4a91-a5d5-4efe0bb67c50"}, :state "Ready"}, :timestamp "2018-11-21T18:47:26.797Z"}
                    {:severity "medium", :id "event/7aa2f88f-d589-4ccf-b98d-11b11e3a2c4f", :type "state", :content {:resource {:href "deployment/55976d06-bfff-4a91-a5d5-4efe0bb67c50"}, :state "SendingReports"}, :timestamp "2018-11-21T18:47:25.670Z"}
                    {:severity "medium", :id "event/8ae764bc-d0ae-4941-89b1-0ac17b016158", :type "state", :content {:resource {:href "deployment/55976d06-bfff-4a91-a5d5-4efe0bb67c50"}, :state "Executing"}, :timestamp "2018-11-21T18:47:00.670Z"}
                    {:severity "medium", :id "event/84995f33-8bb3-45a3-8001-ec47215282cb", :type "action", :content {:resource {:href "deployment/55976d06-bfff-4a91-a5d5-4efe0bb67c50"}, :state "starting deployment/55976d06-bfff-4a91-a5d5-4efe0bb67c50 with async job/5d1e60cf-e303-4f61-93a1-fbe0733dd04c"}, :timestamp "2018-11-21T18:44:36.397Z"}
                    {:severity "medium", :id "event/b21b5b53-63f3-46d0-a846-db090bedafd8", :type "action", :content {:resource {:href "deployment/55976d06-bfff-4a91-a5d5-4efe0bb67c50"}, :state "deployment/55976d06-bfff-4a91-a5d5-4efe0bb67c50 created"}, :timestamp "2018-11-21T18:44:18.805Z"}]
        state-events (filter #(-> % :type (= "state")) events-dev)
        events-map (into {} (map (juxt (comp :state :content) identity) events-dev))
        active-state-index (or (some->> state-events first :content :state (get states-map)) 0)]
    (map (partial step-items @locale active-state-index events-map) steps)))

(defn progession-section
  []
  (let [events (subscribe [::deployment-detail-subs/events])]
    [ui/StepGroup {:fluid  true,
                   :widths (count steps)
                   :items  (extract-steps @events)}]))


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
        [progession-section]
        [summary-section]
        [global-parameters-section]
        [events-section]
        [reports-section resource-id]
        [node-parameters-modal]
        ]])))
