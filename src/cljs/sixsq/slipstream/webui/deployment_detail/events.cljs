(ns sixsq.slipstream.webui.deployment-detail.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.deployment-detail.spec :as deployment-detail-spec]
    [sixsq.slipstream.webui.deployment-detail.effects :as deployment-detail-fx]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.main.effects :as main-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.utils.general :as general-utils]))

(reg-event-fx
  ::set-runUUID
  (fn [{{:keys [::deployment-detail-spec/runUUID] :as db} :db} [_ uuid]]
    {:db (assoc db ::deployment-detail-spec/runUUID uuid)}))


(reg-event-db
  ::set-reports
  (fn [db [_ reports]]
    (assoc db ::deployment-detail-spec/reports reports)))


(reg-event-fx
  ::fetch-reports
  (fn [{{:keys [::client-spec/client
                ::deployment-detail-spec/runUUID] :as db} :db} _]
    (let [filter-str (str "objectType='report' and runUUID='" runUUID "'")
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
  (fn [db [_ resource-id resource]]
    (assoc db ::deployment-detail-spec/loading? false
              ::deployment-detail-spec/cached-resource-id resource-id
              ::deployment-detail-spec/resource resource)))


(reg-event-fx
  ::get-deployment
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id]]
    (when client
      {:db (assoc db ::deployment-detail-spec/loading? true)
       ::deployment-detail-fx/get-deployment
           [client resource-id #(if (instance? js/Error %)
                                  (do
                                    (dispatch [::main-events/set-message {:header  "Failure"
                                                                          :content (->> % ex-data :body)
                                                                          :type    :error}])
                                    (dispatch [::history-events/navigate "deployment"]))
                                  (dispatch [::set-deployment resource-id %]))]})))


(reg-event-db
  ::set-events
  (fn [db [_ events]]
    (assoc db ::deployment-detail-spec/events events)))


(reg-event-fx
  ::fetch-events
  (fn [{{:keys [::client-spec/client
                ::deployment-detail-spec/runUUID] :as db} :db} _]
    (let [filter-str (str "content/resource/href='run/" runUUID "'")
          order-by-str "timestamp:asc"
          select-str "id, content, severity, timestamp, type"
          query-params {:$filter  filter-str
                        :$orderby order-by-str
                        :$select  select-str}]
      {::cimi-api-fx/search [client
                             "events"
                             (general-utils/prepare-params query-params)
                             #(dispatch [::set-events %])]})))
