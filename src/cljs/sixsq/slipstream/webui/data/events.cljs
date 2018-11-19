(ns sixsq.slipstream.webui.data.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.data.effects :as fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.data.spec :as spec]
    [sixsq.slipstream.webui.data.spec :as spec]
    [sixsq.slipstream.webui.data.utils :as utils]

    [taoensso.timbre :as log]))




(reg-event-db
  ::set-time-period
  (fn [db [_ time-period]]
    (assoc db ::spec/time-period time-period
              ::spec/time-period-filter (utils/create-time-period-filter time-period))))


(reg-event-db
  ::set-service-offers
  (fn [db [_ service-offers]]
    (assoc db ::spec/service-offers service-offers)))


(reg-event-db
  ::set-data
  (fn [db [_ data-query-id response]]
    (let [doc-count (get-in response [:aggregations :count:id :value])]
      (update db ::spec/data assoc data-query-id doc-count))))


(reg-event-fx
  ::set-credentials
  (fn [{{:keys [::client-spec/client
                ::spec/time-period-filter
                ::spec/data-queries] :as db} :db} [_ {:keys [credentials]}]]
    (let [cloud-filter (utils/create-cloud-filter credentials)]
      (when client
        (cond-> {:db (assoc db ::spec/credentials credentials
                               ::spec/cloud-filter cloud-filter
                               ::spec/data nil)}
                (not-empty credentials) (assoc ::fx/fetch-data
                                               [client time-period-filter cloud-filter (vals data-queries)
                                                #(dispatch [::set-data %1 %2])]))))))


(reg-event-fx
  ::get-credentials
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    (when client
      {:db                  (assoc db ::spec/credentials nil)
       ::cimi-api-fx/search [client "credentials" {:$filter "type^='cloud-cred'"
                                                   :$select "id, name, connector"}
                             #(dispatch [::set-credentials %])]})))


(reg-event-db
  ::set-applications
  (fn [db [_ applications]]
    (assoc db ::spec/applications (:modules applications)
              ::spec/loading-applications? false)))


(reg-event-fx
  ::open-application-select-modal
  (fn [{{:keys [::client-spec/client
                ::spec/data-queries] :as db} :db} [_ data-query-id]]
    (let [{:keys [query-data query-application]} (get data-queries data-query-id)]
      {:db                  (assoc db ::spec/application-select-visible? true
                                      ::spec/loading-applications? true
                                      ::spec/content-type-filter query-data)
       ::cimi-api-fx/search [client "modules" {:$filter query-application}
                             #(dispatch [::set-applications %])]
       })))


(reg-event-db
  ::close-application-select-modal
  (fn [db _]
    (assoc db ::spec/applications nil
              ::spec/application-select-visible? false)))

