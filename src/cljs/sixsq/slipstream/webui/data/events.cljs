(ns sixsq.slipstream.webui.data.events
  (:require
    [clojure.string :as s]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.data.effects :as fx]
    [sixsq.slipstream.webui.data.spec :as spec]
    [sixsq.slipstream.webui.data.spec :as spec]
    [sixsq.slipstream.webui.data.utils :as utils]
    [taoensso.timbre :as log]))


(defn fetch-data-cofx
  [credentials client time-period-filter cloud-filter full-text-search data-queries]
  (if (empty? credentials)
    {}
    {::fx/fetch-data [client time-period-filter cloud-filter full-text-search (vals data-queries)
                      #(dispatch [::set-data %1 %2])]}))


(reg-event-fx
  ::set-time-period
  (fn [{{:keys [::client-spec/client
                ::spec/cloud-filter
                ::spec/credentials
                ::spec/data-queries
                ::spec/full-text-search] :as db} :db} [_ time-period]]
    (let [time-period-filter (utils/create-time-period-filter time-period)]
      (merge {:db (assoc db ::spec/time-period time-period
                            ::spec/time-period-filter time-period-filter)}
             (fetch-data-cofx credentials client time-period-filter cloud-filter full-text-search data-queries)))))


(reg-event-fx
  ::set-full-text-search
  (fn [{{:keys [::client-spec/client
                ::spec/cloud-filter
                ::spec/credentials
                ::spec/data-queries
                ::spec/time-period-filter] :as db} :db} [_ full-text-search]]
    (let [full-text-query (when (and full-text-search (not (str/blank? full-text-search)))
                            (str "fulltext=='" full-text-search "*'"))]
      (merge {:db (assoc db ::spec/full-text-search full-text-query)}
             (fetch-data-cofx credentials client time-period-filter cloud-filter full-text-query data-queries)))))

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
                ::spec/data-queries
                ::spec/full-text-search] :as db} :db} [_ {:keys [credentials]}]]
    (let [cloud-filter (utils/create-cloud-filter credentials)]
      (when client
        (merge {:db (assoc db ::spec/credentials credentials
                              ::spec/cloud-filter cloud-filter
                              ::spec/data nil)}
               (fetch-data-cofx credentials client time-period-filter cloud-filter full-text-search data-queries))))))


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
                ::spec/data-queries
                ::spec/datasets] :as db} :db} [_ data-query-id]]
    (let [selected-data-queries (filter (fn [[k v]] (boolean (datasets k))) )
          {:keys [query-data query-application]} (get data-queries data-query-id)]
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


(reg-event-db
  ::add-dataset
  (fn [{:keys [::spec/datasets] :as db} [_ id]]
    (log/error "adding dataset" id datasets)
    (assoc db ::spec/datasets (conj datasets id))))


(reg-event-db
  ::remove-dataset
  (fn [{:keys [::spec/datasets] :as db} [_ id]]
    (log/error "removing dataset" id datasets)
    (assoc db ::spec/datasets (disj datasets id))))



