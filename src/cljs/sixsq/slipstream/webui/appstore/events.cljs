(ns sixsq.slipstream.webui.appstore.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.appstore.spec :as spec]
    [sixsq.slipstream.webui.appstore.spec :as spec]
    [sixsq.slipstream.webui.appstore.utils :as utils]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.history.events :as history-evts]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.response :as response]
    [taoensso.timbre :as log]))


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
      {:db                  (assoc db ::spec/modules nil)
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


(reg-event-db
  ::set-selected-credential
  (fn [{:keys [::spec/deployment
               ::spec/credentials] :as db} [_ {:keys [id] :as credential}]]
    (let [updated-deployment (utils/update-parameter-in-deployment "credential.id" id deployment)]
      (assoc db ::spec/selected-credential credential
                ::spec/deployment updated-deployment))))


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
  (fn [{{:keys [::client-spec/client] :as db} :db :as cofx} [_ depl-tmpl-id]]
    (when client
      (let [data {:name               (str "Deployment from " depl-tmpl-id)
                  :description        (str "A deployment for the deployment template " depl-tmpl-id)
                  :deploymentTemplate {:href depl-tmpl-id}}
            add-depl-callback (fn [response]
                                (if (instance? js/Error response)
                                  (let [{:keys [status message]} (response/parse-ex-info response)]
                                    (dispatch [::messages-events/add
                                               {:header  (cond-> (str "error create deployment")
                                                                 status (str " (" status ")"))
                                                :content message
                                                :type    :error}])
                                    (dispatch [::close-deploy-modal]))
                                  (dispatch [::get-deployment (:resource-id response)])))
            search-creds-callback #(dispatch [::set-credentials (get % :credentials [])])]
        {:db                  (assoc db ::spec/loading-deployment? true
                                        ::spec/loading-credentials? true
                                        ::spec/credentials nil
                                        ::spec/selected-credential nil
                                        ::spec/deploy-modal-visible? true
                                        ::spec/step-id "summary")
         ::cimi-api-fx/add    [client "deployments" data add-depl-callback]
         ::cimi-api-fx/search [client "credentials" {:$filter (str "type^='cloud-cred'")} search-creds-callback]}))))


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

