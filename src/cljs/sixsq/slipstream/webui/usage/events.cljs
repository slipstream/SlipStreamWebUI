(ns sixsq.slipstream.webui.usage.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.usage.effects :as usage-fx]
    [sixsq.slipstream.webui.usage.spec :as usage-spec]
    [sixsq.slipstream.webui.usage.utils :as u]
    [clojure.string :as str]
    [taoensso.timbre :as log]))


(defn get-credentials-map-cofx
  [client selected-users-roles]
  (let [users-roles-filter (->>
                             selected-users-roles
                             (map #(str "acl/owner/principal = '" % "' or acl/rules/principal = '" % "'"))
                             (str/join " or "))
        filter-str (cond-> "type^='cloud-cred-'"
                           (not-empty selected-users-roles) (str " and (" users-roles-filter ")"))]
    {::cimi-api-fx/search [client
                           :credentials
                           {:$select "id,name,description,connector"
                            :$filter filter-str}
                           #(dispatch [::set-credentials-map %])]}))


(reg-event-fx
  ::get-credentials-map
  (fn [{{:keys [::client-spec/client
                ::usage-spec/selected-users-roles] :as db} :db} _]
    (get-credentials-map-cofx client selected-users-roles)))


(reg-event-db
  ::set-credentials-map
  (fn [db [_ response]]
    (let [credentials (get response :credentials [])
          map_id_cred (->> credentials
                           (map #(vector (:id %) %))
                           (into {}))]
      (-> db
          (assoc ::usage-spec/credentials-map map_id_cred)
          (assoc ::usage-spec/loading-credentials-map? false)))))


(reg-event-db
  ::set-selected-credentials
  (fn [db [_ credentials]]
    (assoc db ::usage-spec/selected-credentials credentials)))


(reg-event-fx
  ::set-users-roles
  (fn [{{:keys [::client-spec/client
                ::usage-spec/selected-users-roles] :as db} :db} [_ user]]
    (merge {:db (-> db
                    (assoc ::usage-spec/selected-users-roles user)
                    (assoc ::usage-spec/loading-credentials-map? true))}
           (get-credentials-map-cofx client user))))


(reg-event-fx
  ::clear-user
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    (merge {:db (-> db
                    (assoc ::usage-spec/selected-users-roles nil)
                    (assoc ::usage-spec/loading-credentials-map? true))}
           (get-credentials-map-cofx client nil))))


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
                ::usage-spec/credentials-map] :as db} :db}]
    {:db                        (assoc db ::usage-spec/loading? true)
     ::usage-fx/fetch-meterings [client
                                 (-> date-range first .clone .utc .format)
                                 (-> date-range second .clone .utc .format)
                                 (conj (if (empty? selected-credentials)
                                         (keys credentials-map)
                                         selected-credentials) u/all-credentials)
                                 #(dispatch [::set-results %])]}))


(reg-event-db
  ::toggle-filter
  (fn [{:keys [::usage-spec/filter-visible?] :as db} _]
    (assoc db ::usage-spec/filter-visible? (not filter-visible?))))


