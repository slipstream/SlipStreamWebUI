(ns sixsq.slipstream.webui.deployment.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.deployment.spec :as spec]
    [taoensso.timbre :as log]
    [clojure.string :as str]))


(reg-event-db
  ::set-deployments-creds-map
  (fn [db [_ deployments-creds-map]]
    (assoc db ::spec/deployments-creds-map deployments-creds-map)))


(reg-event-db
  ::set-deployments-service-url-map
  (fn [db [_ deployments-service-url-map]]
    (assoc db ::spec/deployments-service-url-map deployments-service-url-map)))


(reg-event-fx
  ::set-deployments
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ deployments]]
    (let [deployments-resource-ids (->> deployments :deployments (map :id))
          filter-deps-ids (str/join " or " (map #(str "deployment/href='" % "'") deployments-resource-ids))
          query-params {:$filter (str "(" filter-deps-ids
                                      ") and (name='credential.id' or name='ss:url.service') and value!=null")
                        :$select "id, deployment, name, value"}
          callback (fn [response]
                     (when-not (instance? js/Error response)
                       (let [deployment-params (->> response :deploymentParameters (group-by :name))
                             deployments-creds-map (->> (get deployment-params "credential.id")
                                                        (group-by (comp :href :deployment))
                                                        (map (fn [[k param-list]]
                                                               [k (->> param-list (map :value) set)]))
                                                        (into {}))
                             deployments-service-url-map (->> (get deployment-params "ss:url.service")
                                                              (map (juxt (comp :href :deployment) :value))
                                                              (into {}))]
                         (dispatch [::set-deployments-creds-map deployments-creds-map])
                         (dispatch [::set-deployments-service-url-map deployments-service-url-map]))))]
      (cond-> {:db (assoc db ::spec/loading? false
                             ::spec/deployments deployments)}
              (not-empty deployments-resource-ids) (assoc ::cimi-api-fx/search
                                                          [client "deploymentParameters" query-params callback])))))


(defn get-query-params
  [full-text-search active-only? page elements-per-page]
  (let [filter-active-only? (when active-only? "state!='STOPPED'")
        full-text-search (when-not (str/blank? full-text-search) (str "description=='" full-text-search "*'"))
        filter (str/join " and " (remove nil? [filter-active-only? full-text-search]))]
    (cond-> {:$first   (inc (* (dec page) elements-per-page))
             :$last    (* page elements-per-page)
             :$orderby "created:desc"}
            (not (str/blank? filter)) (assoc :$filter filter))))


(reg-event-fx
  ::get-deployments
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/active-only?
                ::spec/page
                ::spec/elements-per-page] :as db} :db} _]
    {:db                  (assoc db ::spec/loading? true)
     ::cimi-api-fx/search [client "deployments" (get-query-params full-text-search active-only? page elements-per-page)
                           #(dispatch [::set-deployments %])]}))


(reg-event-fx
  ::set-page
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/page
                ::spec/active-only?
                ::spec/elements-per-page] :as db} :db} [_ page]]
    {:db                  (assoc db ::spec/page page)
     ::cimi-api-fx/search [client "deployments" (get-query-params full-text-search active-only? page elements-per-page)
                           #(dispatch [::set-deployments %])]}))


(reg-event-fx
  ::set-active-only?
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/page
                ::spec/elements-per-page] :as db} :db} [_ active-only?]]
    {:db                  (-> db
                              (assoc ::spec/active-only? active-only?)
                              (assoc ::spec/page 1))
     ::cimi-api-fx/search [client "deployments" (get-query-params full-text-search active-only? page elements-per-page)
                           #(dispatch [::set-deployments %])]}))

(reg-event-fx
  ::set-full-text-search
  (fn [{{:keys [::client-spec/client
                ::spec/page
                ::spec/active-only?
                ::spec/elements-per-page] :as db} :db} [_ full-text-search]]
    {:db                  (-> db
                              (assoc ::spec/full-text-search full-text-search)
                              (assoc ::spec/page 1))
     ::cimi-api-fx/search [client "deployments" (get-query-params full-text-search active-only? page elements-per-page)
                           #(dispatch [::set-deployments %])]}))