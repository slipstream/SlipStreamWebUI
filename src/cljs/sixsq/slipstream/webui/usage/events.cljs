(ns sixsq.slipstream.webui.usage.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.usage.effects :as usage-fx]
    [sixsq.slipstream.webui.usage.spec :as usage-spec]
    [sixsq.slipstream.webui.usage.utils :as u]))


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


(defn get-credentials-list-cofx
  [client user]
  (let [filter-str (cond-> "type^='cloud-cred-'"
                           user (str " and (acl/owner/principal = '" user "' or acl/rules/principal = '" user "')"))]

    {::cimi-api-fx/search [client
                           :credentials
                           {:$select "id,name,description,connector"
                            :$filter filter-str}
                           #(dispatch [::set-credentials-list %])]}))


(reg-event-fx
  ::get-credentials-list
  (fn [{{:keys [::client-spec/client
                ::usage-spec/selected-user] :as db} :db} _]
    (get-credentials-list-cofx client selected-user)))


(reg-event-db
  ::set-credentials-list
  (fn [db [_ response]]
    (let [credentials (get response :credentials [])
          map_id_cred (->> credentials
                           (map #(vector (:id %) %))
                           (into {}))]
      (-> db
          (assoc ::usage-spec/credentials-list map_id_cred)
          (assoc ::usage-spec/loading-credentials-list? false)))))


(reg-event-db
  ::set-selected-credentials
  (fn [db [_ credentials]]
    (assoc db ::usage-spec/selected-credentials credentials)))


(reg-event-fx
  ::set-user
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ user]]
    (merge {:db (-> db
                    (assoc ::usage-spec/selected-user user)
                    (assoc ::usage-spec/loading-credentials-list? true))}
           (get-credentials-list-cofx client user))))


(reg-event-fx
  ::clear-user
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    (merge {:db (-> db
                    (assoc ::usage-spec/selected-user nil)
                    (assoc ::usage-spec/loading-credentials-list? true))}
           (get-credentials-list-cofx client nil))))


(reg-event-db
  ::set-date-range
  (fn [db [_ date-range]]
    (assoc db ::usage-spec/date-range date-range)))


(reg-event-db
  ::set-results
  (fn [db [_ results]]
    (-> db
        (assoc ::usage-spec/results results)
        (assoc ::usage-spec/loading? false))))


(reg-event-fx
  ::fetch-meterings
  (fn [{{:keys [::client-spec/client
                ::usage-spec/date-range
                ::usage-spec/selected-credentials
                ::usage-spec/credentials-list
                ::usage-spec/selected-user] :as db} :db}]
    {:db                        (assoc db ::usage-spec/loading? true)
     ::usage-fx/fetch-meterings [client
                                 (-> date-range first .clone .utc .format)
                                 (-> date-range second .clone .utc .format)
                                 selected-user
                                 (conj (if (empty? selected-credentials)
                                         (keys credentials-list)
                                         selected-credentials) u/all-credentials)
                                 #(dispatch [::set-results %])]}))


(reg-event-db
  ::toggle-filter
  (fn [{:keys [::usage-spec/filter-visible?] :as db} _]
    (assoc db ::usage-spec/filter-visible? (not filter-visible?))))


