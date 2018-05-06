(ns sixsq.slipstream.webui.usage.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]
    [sixsq.slipstream.webui.usage.spec :as usage-spec]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.usage.effects :as usage-fx]))

(reg-event-db
  ::set-connectors-list
  (fn [db [_ {:keys [connectors] :as response}]]
    (-> db
        (assoc ::usage-spec/connectors-list (map :id connectors))
        (assoc ::usage-spec/loading-connectors-list? false))))

(reg-event-db
  ::set-selected-connectors
  (fn [db [_ connectors]]
    (assoc db ::usage-spec/selected-connectors connectors)))

(reg-event-fx
  ::get-connectors-list
  (fn [{{:keys [::client-spec/client
                ::cimi-spec/cloud-entry-point] :as db} :db} _]
    (let [resource-type (-> cloud-entry-point
                            :collection-key
                            (get "connector"))]
      {::cimi-api-fx/search [client
                             resource-type
                             {:$orderby "id"
                              :$select  "id"}
                             #(dispatch [::set-connectors-list %])]})))

(reg-event-fx
  ::get-users-list
  (fn [{{:keys [::client-spec/client
                ::cimi-spec/cloud-entry-point] :as db} :db} _]
    (let [resource-type (-> cloud-entry-point
                            :collection-key
                            (get "user"))]
      {::cimi-api-fx/search [client
                             resource-type
                             {:$select "id"}
                             #(dispatch [::set-users-list %])]})))

(reg-event-db
  ::set-users-list
  (fn [db [_ response]]
    (let [users (map :id (get response :users []))]
      (-> db
          (assoc ::usage-spec/users-list users)
          (assoc ::usage-spec/loading-users-list? false)))))

(reg-event-db
  ::set-user
  (fn [db [_ user]]
    (assoc db ::usage-spec/selected-user user)))

(reg-event-db
  ::clear-user
  (fn [db [_ user]]
    (assoc db ::usage-spec/selected-user nil)))

(reg-event-db
  ::set-date-after-before
  (fn [db [_ date-after date-before]]
    (-> db
        (assoc ::usage-spec/date-after date-after)
        (assoc ::usage-spec/date-before date-before))))

(reg-event-db
  ::set-results
  (fn [db [_ results]]
    (-> db
        (assoc ::usage-spec/results results)
        (assoc ::usage-spec/loading? false))))

(reg-event-fx
  ::fetch-meterings
  (fn [{{:keys [::client-spec/client
                ::usage-spec/date-after
                ::usage-spec/date-before
                ::usage-spec/selected-connectors
                ::usage-spec/connectors-list
                ::usage-spec/selected-user] :as db} :db}]
    {:db                        (assoc db ::usage-spec/loading? true)
     ::usage-fx/fetch-meterings [client
                                 (-> date-after .clone .utc .format)
                                 (-> date-before .clone .utc .format)
                                 selected-user
                                 (conj (if (empty? selected-connectors)
                                         connectors-list
                                         selected-connectors) "all-clouds")
                                 #(dispatch [::set-results %])]}))


(reg-event-db
  ::toggle-filter
  (fn [{:keys [::usage-spec/filter-visible?] :as db} _]
    (assoc db ::usage-spec/filter-visible? (not filter-visible?))))


