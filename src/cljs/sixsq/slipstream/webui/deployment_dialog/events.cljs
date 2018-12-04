(ns sixsq.slipstream.webui.deployment-dialog.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.data.spec :as data-spec]
    [sixsq.slipstream.webui.data.utils :as data-utils]
    [sixsq.slipstream.webui.deployment-dialog.spec :as spec]
    [sixsq.slipstream.webui.deployment-dialog.utils :as utils]
    [sixsq.slipstream.webui.history.events :as history-evts]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.response :as response]))


(reg-event-fx
  ::close-deploy-modal
  (fn [{{:keys [::client-spec/client
                ::spec/deployment] :as db} :db :as cofx} _]
    (cond-> {:db                  (assoc db ::spec/deploy-modal-visible? false
                                            ::spec/deployment nil)
             ::cimi-api-fx/delete [client (:id deployment) #()]})))


(reg-event-db
  ::set-credentials
  (fn [db [_ credentials]]
    (assoc db ::spec/credentials credentials
              ::spec/loading-credentials? false)))


(reg-event-fx
  ::set-selected-credential
  (fn [{{:keys [::client-spec/client
                ::spec/deployment
                ::data-spec/time-period-filter
                ::spec/cloud-filter
                ::data-spec/content-type-filter] :as db} :db} [_ {:keys [id] :as credential} summary-item]]
    (let [updated-deployment (utils/update-parameter-in-deployment "credential.id" id deployment)
          filter (data-utils/join-and time-period-filter cloud-filter content-type-filter)
          callback-data #(when-let [service-offers-ids (seq (map :id (:serviceOffers %)))]
                           (dispatch [::set-deployment
                                      (assoc updated-deployment :serviceOffers service-offers-ids)]))]
      (cond-> {:db (-> db
                       (assoc ::spec/selected-credential credential
                              ::spec/deployment updated-deployment)
                       (assoc-in [::spec/step-states :credentials :summary] summary-item))}
              cloud-filter (assoc ::cimi-api-fx/search [client "serviceOffers"
                                                        {:$filter filter, :$select "id"}
                                                        callback-data])))))


(reg-event-db
  ::set-active-step
  (fn [db [_ active-step]]
    (assoc db ::spec/active-step active-step)))


(reg-event-db
  ::set-deployment
  (fn [db [_ deployment]]
    (assoc db ::spec/deployment deployment
              ::spec/loading-deployment? false)))


(reg-event-fx
  ::get-deployment
  (fn [{{:keys [::client-spec/client] :as db} :db :as cofx} [_ id]]
    (when client
      {:db               (assoc db ::spec/deployment {:id id})
       ::cimi-api-fx/get [client id #(dispatch [::set-deployment %])]})))


(reg-event-fx
  ::create-deployment
  (fn [{{:keys [::client-spec/client] :as db} :db :as cofx} [_ id first-step]]
    (when client
      (when (= "data" first-step)
        (dispatch [::get-service-offers-by-cred]))
      (let [data (if (str/starts-with? id "module/")
                   {:deploymentTemplate {:module {:href id}}}
                   {:name               (str "Deployment from " id)
                    :description        (str "A deployment for the deployment template " id)
                    :deploymentTemplate {:href id}})
            add-depl-callback (fn [response]
                                (if (instance? js/Error response)
                                  (let [{:keys [status message]} (response/parse-ex-info response)]
                                    (dispatch [::messages-events/add
                                               {:header  (cond-> (str "error create deployment")
                                                                 status (str " (" status ")"))
                                                :content message
                                                :type    :error}])
                                    (dispatch [::close-deploy-modal]))
                                  (dispatch [::get-deployment (:resource-id response)])))]
        {:db               (assoc db ::spec/loading-deployment? true
                                     ::spec/selected-credential nil
                                     ::spec/deploy-modal-visible? true
                                     ::spec/active-step (or first-step "data")
                                     ::spec/cloud-filter nil
                                     ::spec/selected-cloud nil
                                     ::spec/connectors nil
                                     ::spec/data-clouds nil)
         ::cimi-api-fx/add [client "deployments" data add-depl-callback]}))))


(reg-event-fx
  ::get-credentials
  (fn [{{:keys [::client-spec/client
                ::spec/cloud-filter] :as db} :db :as cofx} _]
    (when client
      (let [search-creds-callback #(dispatch [::set-credentials (get % :credentials [])])]
        {:db                  (assoc db ::spec/loading-credentials? true
                                        ::spec/credentials nil
                                        ::spec/selected-credential nil)
         ::cimi-api-fx/search [client "credentials"
                               {:$select "id, name, description, created, type"
                                :$filter (data-utils/join-and
                                           cloud-filter
                                           (str "type^='cloud-cred'"))} search-creds-callback]}))))


(reg-event-fx
  ::start-deployment
  (fn [{{:keys [::client-spec/client] :as db} :db :as cofx} [_ id]]
    (when client
      (let [start-callback (fn [response]
                             (if (instance? js/Error response)
                               (let [{:keys [status message]} (response/parse-ex-info response)]
                                 (dispatch [::messages-events/add
                                            {:header  (cond-> (str "error start " id)
                                                              status (str " (" status ")"))
                                             :content message
                                             :type    :error}]))
                               (let [{:keys [status message resource-id]} (response/parse response)
                                     success-msg {:header  (cond-> (str "started " resource-id)
                                                                   status (str " (" status ")"))
                                                  :content message
                                                  :type    :success}]
                                 (dispatch [::messages-events/add success-msg])
                                 (dispatch [::history-evts/navigate id]))))]
        {:db                     (assoc db ::spec/deploy-modal-visible? false)
         ::cimi-api-fx/operation [client id "http://schemas.dmtf.org/cimi/2/action/start" start-callback]}))))


(reg-event-fx
  ::edit-deployment
  (fn [{{:keys [::client-spec/client
                ::spec/deployment] :as db} :db :as cofx} _]
    (when client
      (let [resource-id (:id deployment)
            edit-callback (fn [response]
                            (if (instance? js/Error response)
                              (let [{:keys [status message]} (response/parse-ex-info response)]
                                (dispatch [::messages-events/add
                                           {:header  (cond-> (str "error editing " resource-id)
                                                             status (str " (" status ")"))
                                            :content message
                                            :type    :error}]))
                              (dispatch [::start-deployment resource-id])))]
        {::cimi-api-fx/edit [client resource-id deployment edit-callback]}))))


(reg-event-db
  ::set-connectors
  (fn [db [_ {:keys [connectors]}]]
    (assoc db ::spec/connectors (into {} (map (juxt :id identity) connectors)))))


(defn set-cloud-and-filter
  [db cloud]
  (assoc db ::spec/selected-cloud cloud
            ::spec/cloud-filter (str "(connector/href='" cloud "' or connector/href='connector/" cloud "')")))


(reg-event-fx
  ::set-data-clouds
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ data-clouds-response]]
    (let [buckets (get-in data-clouds-response [:aggregations (keyword "terms:connector/href") :buckets])
          clouds (map :key buckets)
          filter (apply data-utils/join-or (map #(str "id='" % "'") clouds))]

      {:db                  (cond-> (assoc db ::spec/data-clouds buckets)
                                    (= 1 (count clouds)) (set-cloud-and-filter (first clouds)))
       ::cimi-api-fx/search [client "connectors"
                             {:$filter filter
                              :$select "id, name, description, cloudServiceType"} #(dispatch [::set-connectors %])]})))


(reg-event-fx
  ::get-service-offers-by-cred
  (fn [{{:keys [::client-spec/client
                ::data-spec/time-period-filter
                ::data-spec/cloud-filter
                ::data-spec/content-type-filter
                ::data-spec/credentials] :as db} :db} _]
    (when client

      (let [filter (data-utils/join-and time-period-filter cloud-filter content-type-filter)]
        (-> {:db db}
            (assoc ::cimi-api-fx/search
                   [client "serviceOffers" {:$filter      filter
                                            :$last        0
                                            :$aggregation "terms:connector/href"}
                    #(dispatch [::set-data-clouds %])]))))))


(reg-event-db
  ::set-cloud-filter
  (fn [db [_ cloud summary-item]]
    (-> db
        (set-cloud-and-filter cloud)
        (assoc-in [::spec/step-states :data :summary] summary-item))))


(reg-event-db
  ::set-size-summary
  (fn [db [_ summary-item]]
    (assoc-in db [::spec/step-states :size :summary] summary-item)))


(reg-event-db
  ::set-parameters-summary
  (fn [db [_ summary-item]]
    (assoc-in db [::spec/step-states :parameters :summary] summary-item)))
