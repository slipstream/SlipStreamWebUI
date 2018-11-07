(ns sixsq.slipstream.webui.deployment-detail.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.deployment-detail.effects :as deployment-detail-fx]
    [sixsq.slipstream.webui.deployment-detail.spec :as spec]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.main.effects :as main-fx]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [sixsq.slipstream.webui.utils.response :as response]
    [taoensso.timbre :as log]
    [clojure.string :as str]))


(reg-event-db
  ::set-runUUID
  (fn [{:keys [::spec/runUUID] :as db} [_ uuid]]
    (assoc db ::spec/runUUID uuid)))


(reg-event-db
  ::set-reports
  (fn [db [_ reports]]
    (assoc db ::spec/reports reports)))


(reg-event-fx
  ::get-reports
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ href]]
    (let [filter-str (str "objectType='report' and runUUID='" href "'")
          order-by-str "created:desc, component"
          select-str "id, state, created, component"
          query-params {:$filter  filter-str
                        :$orderby order-by-str
                        :$select  select-str}]
      {::cimi-api-fx/search [client
                             "externalObjects"
                             (general-utils/prepare-params query-params)
                             #(dispatch [::set-reports %])]})))


(reg-event-fx
  ::download-report
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ id]]
    {::cimi-api-fx/operation [client id "http://sixsq.com/slipstream/1/action/download"
                              #(dispatch [::download (:uri %)])]}))


(reg-event-fx
  ::download
  (fn [_ [_ uri]]
    {::main-fx/open-new-window [uri]}))


(reg-event-db
  ::set-deployment
  (fn [db [_ resource]]
    (assoc db ::spec/loading? false
              ::spec/deployment resource)))

(reg-event-db
  ::set-deployment-parameters
  (fn [db [_ resources]]
    (assoc db ::spec/global-deployment-parameters (get resources :deploymentParameters []))))


(reg-event-fx
  ::get-global-deployment-parameters
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id]]
    (let [filter-depl-params {:$filter  (str "deployment/href='" resource-id "' and nodeID=null")
                              :$orderby "name"}
          get-depl-params-callback #(dispatch [::set-deployment-parameters %])]
      {::cimi-api-fx/search [client "deploymentParameters" filter-depl-params get-depl-params-callback]})))


(reg-event-fx
  ::get-deployment
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id]]
    (log/error "::get-deployment href: " resource-id)
    (when client
      (let [get-depl-callback #(if (instance? js/Error %)
                                 (let [{:keys [status message]} (response/parse-ex-info %)]
                                   (dispatch [::messages-events/add
                                              {:header  (cond-> (str "error getting deployment " resource-id)
                                                                status (str " (" status ")"))
                                               :content message
                                               :type    :error}])
                                   (dispatch [::history-events/navigate "deployment"]))
                                 (dispatch [::set-deployment %]))]
        {:db               (assoc db ::spec/loading? true)
         ::cimi-api-fx/get [client resource-id get-depl-callback]}))))


(reg-event-fx
  ::stop-deployment
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ href]]
    {:db                     db
     ::cimi-api-fx/operation [client href "http://schemas.dmtf.org/cimi/2/action/stop"
                              #(if (instance? js/Error %)
                                 (let [{:keys [status message]} (response/parse-ex-info %)]
                                   (dispatch [::messages-events/add
                                              {:header  (cond-> (str "error stopping deployment " href)
                                                                status (str " (" status ")"))
                                               :content message
                                               :type    :error}]))
                                 (dispatch [::get-deployment href])
                                 )]}))



(reg-event-db
  ::set-events
  (fn [db [_ events]]
    (assoc db ::spec/events events)))


(reg-event-fx
  ::get-events
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ href]]
    (let [filter-str (str "content/resource/href='" href "'")
          order-by-str "timestamp:asc"
          select-str "id, content, severity, timestamp, type"
          query-params {:$filter  filter-str
                        :$orderby order-by-str
                        :$select  select-str}]
      {::cimi-api-fx/search [client
                             "events"
                             (general-utils/prepare-params query-params)
                             #(dispatch [::set-events %])]})))


(reg-event-fx
  ::open-link
  (fn [_ [_ uri]]
    {::main-fx/open-new-window [uri]}))

(reg-event-db
  ::set-node-parameters
  (fn [db [_ node-parameters]]
    (assoc db ::spec/node-parameters node-parameters)))

(defn get-node-parameters
  [client deployment node-name]
  (let [filter-str (str "deployment/href='" (:id deployment) "' and nodeID='" node-name "'")
        select-str "id, created, updated, name, description, value"
        query-params {:$filter filter-str
                      :$select select-str}]
    {::cimi-api-fx/search [client
                           "deploymentParameters"
                           (general-utils/prepare-params query-params)
                           #(dispatch [::set-node-parameters (:deploymentParameters %)])]}))

(reg-event-db
  ::show-node-parameters-modal
  (fn [{:keys [::client-spec/client
               ::spec/deployment] :as db} [_ node-name]]
    (assoc db ::spec/node-parameters-modal node-name
              ::spec/node-parameters nil)))

(reg-event-fx
  ::get-node-parameters
  (fn [{{:keys [::client-spec/client
                ::spec/deployment
                ::spec/node-parameters-modal] :as db} :db} _]
    (when (boolean node-parameters-modal)
      (let [filter-str (str "deployment/href='" (:id deployment) "' and nodeID='" node-parameters-modal "'")
            select-str "id, created, updated, name, description, value"
            query-params {:$filter  filter-str
                          :$select  select-str
                          :$orderby "name"}]
        {::cimi-api-fx/search [client
                               "deploymentParameters"
                               (general-utils/prepare-params query-params)
                               #(dispatch [::set-node-parameters (:deploymentParameters %)])]}))))

(reg-event-db
  ::close-node-parameters-modal
  (fn [db _]
    (assoc db ::spec/node-parameters-modal nil)))


(reg-event-db
  ::set-summary-nodes-parameters
  (fn [db [_ summary-nodes-parameters]]
    (assoc db ::spec/summary-nodes-parameters (group-by :nodeID summary-nodes-parameters))))


(def summary-param-names #{"statecustom"
                           "url.service"
                           "url.ssh"
                           "password.ssh"
                           "complete"})

(reg-event-fx
  ::get-summary-nodes-parameters
  (fn [{{:keys [::client-spec/client
                ::spec/deployment] :as db} :db} [_ nodes]]
    (when (some? nodes)
      (let [nodes-filter (str/join " or " (map #(str "nodeID='" % "'") nodes))
            names-filter (str/join " or " (map #(str "name='" % "'") summary-param-names))
            filter-str (str/join " and " [(str "deployment/href='" (:id deployment) "'")
                                          (str "(" nodes-filter ")")
                                          (str "(" names-filter ")")])
            select-str "nodeID, name, value"
            query-params {:$filter  filter-str
                          :$select  select-str
                          :$orderby "name"}]
        {::cimi-api-fx/search [client
                               "deploymentParameters"
                               (general-utils/prepare-params query-params)
                               #(dispatch [::set-summary-nodes-parameters (:deploymentParameters %)])]}))))
