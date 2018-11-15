(ns sixsq.slipstream.webui.appstore.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.appstore.spec :as spec]
    [sixsq.slipstream.webui.data.spec :as data-spec]
    [sixsq.slipstream.webui.appstore.utils :as utils]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.history.events :as history-evts]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.response :as response]
    [taoensso.timbre :as log]
    [clojure.string :as str]
    [sixsq.slipstream.webui.data.utils :as data-utils]))


(reg-event-db
  ::set-deployment-templates
  (fn [db [_ deployment-templates]]
    (assoc db ::spec/deployment-templates deployment-templates)))


(defn get-query-params
  [full-text-search page elements-per-page]
  (cond-> {:$first (inc (* (dec page) elements-per-page))
           :$last  (* page elements-per-page)}
          (not-empty full-text-search) (assoc :$filter (str "description=='" full-text-search "*'"))))


(reg-event-fx
  ::set-full-text-search
  (fn [{{:keys [::client-spec/client
                ::spec/elements-per-page] :as db} :db} [_ full-text-search]]
    (let [new-page 1]
      {:db                  (assoc db ::spec/full-text-search full-text-search
                                      ::spec/page new-page)
       ::cimi-api-fx/search [client "deploymentTemplates" (get-query-params full-text-search new-page elements-per-page)
                             #(dispatch [::set-deployment-templates %])]})))


(reg-event-fx
  ::get-deployment-templates
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/page
                ::spec/elements-per-page] :as db} :db} _]
    (when client
      {:db                  (assoc db ::spec/deployment-templates nil)
       ::cimi-api-fx/search [client "deploymentTemplates" (get-query-params full-text-search page elements-per-page)
                             #(dispatch [::set-deployment-templates %])]})))


(reg-event-fx
  ::set-page
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/page
                ::spec/elements-per-page] :as db} :db} [_ page]]
    {:db                  (assoc db ::spec/page page)
     ::cimi-api-fx/search [client "deploymentTemplates" (get-query-params full-text-search page elements-per-page)
                           #(dispatch [::set-deployment-templates %])]}))


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
                ::data-spec/gnss-filter
                ::spec/cloud-filter
                ::data-spec/content-type-filter] :as db} :db} [_ {:keys [id] :as credential}]]
    (let [updated-deployment (utils/update-parameter-in-deployment "credential.id" id deployment)
          filter (data-utils/join-filters time-period-filter cloud-filter gnss-filter content-type-filter)
          callback-data #(when-let [service-offers-ids (seq (map :id (:serviceOffers %)))]
                           (dispatch
                             [::set-deployment
                              (assoc updated-deployment :serviceOffers service-offers-ids)]))]
      {:db                  (assoc db ::spec/selected-credential credential
                                      ::spec/deployment updated-deployment)
       ::cimi-api-fx/search [client "serviceOffers" {:$filter filter :$select "id"} callback-data]})))


(reg-event-db
  ::set-step-id
  (fn [db [_ step-id]]
    (assoc db ::spec/step-id step-id)))


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
  (fn [{{:keys [::client-spec/client] :as db} :db :as cofx} [_ id]]
    (when client
      (dispatch [::get-service-offers-by-cred])
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
                                     ::spec/step-id "summary"
                                     ::spec/cloud-filter nil)
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
                               {:$filter (data-utils/join-filters
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
  ::set-data-clouds
  (fn [db [_ data-clouds-response]]
    (let [buckets (get-in data-clouds-response [:aggregations (keyword "terms:connector/href") :buckets])]
      (assoc db ::spec/data-clouds buckets))))


(reg-event-fx
  ::get-service-offers-by-cred
  (fn [{{:keys [::client-spec/client
                ::data-spec/time-period-filter
                ::data-spec/cloud-filter
                ::data-spec/gnss-filter
                ::data-spec/content-type-filter
                ::data-spec/credentials] :as db} :db} _]
    (when client
      (let [filter (data-utils/join-filters time-period-filter cloud-filter gnss-filter content-type-filter)]
        (log/error filter)
        (-> {:db db}
            (assoc ::cimi-api-fx/search
                   [client "serviceOffers" {:$filter      filter
                                            :$last        0
                                            :$aggregation "terms:connector/href"}
                    #(dispatch [::set-data-clouds %])]))))))

(reg-event-db
  ::set-cloud-filter
  (fn [db [_ cloud]]
    (assoc db ::spec/cloud-filter (str "(connector/href='" cloud "' or connector/href='connector/" cloud "')"))))

