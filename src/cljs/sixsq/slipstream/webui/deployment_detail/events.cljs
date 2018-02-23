(ns sixsq.slipstream.webui.deployment-detail.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.deployment-detail.spec :as deployment-detail-spec]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.main.effects :as main-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.utils.general :as general-utils]))

(reg-event-fx
  ::set-runUUID
  (fn [{{:keys [::deployment-detail-spec/runUUID] :as db} :db} [_ uuid]]
    {:db (assoc db ::deployment-detail-spec/runUUID uuid)}))


(reg-event-db
  ::set-reports
  (fn [db[_ reports]]
    (assoc db ::deployment-detail-spec/reports reports)))

(reg-event-fx
  ::fetch-reports
  (fn [{{:keys [::client-spec/client
                ::deployment-detail-spec/runUUID] :as db} :db} _]
    (let [filter-str (str "objectType='report' and runUUID='" runUUID "'")
          order-by-str "created:desc, component"
          select-str "id, state, created, component"
          query-params {:$filter filter-str
                        :$orderby order-by-str
                        :$select select-str}]
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
