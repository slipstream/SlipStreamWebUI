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


(reg-event-fx
  ::set-deployments
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ deployments]]
    (let [deployments-resource-ids (->> deployments :deployments (map :id))
          filter-deps-ids (str/join " or " (map #(str "deployment/href='" % "'") deployments-resource-ids))
          query-params {:$filter (str "(" filter-deps-ids ") and name='credential.id' and value!=null")
                        :$select "id, deployment, value"}
          callback (fn [response]
                     (when-not (instance? js/Error response)
                       (let [deployments-creds-map (->> response
                                                        :deploymentParameters
                                                        (group-by (comp :href :deployment))
                                                        (map (fn [[k param-list]]
                                                               [k (->> param-list (map :value) set)]))
                                                        (into {}))]
                         (dispatch [::set-deployments-creds-map deployments-creds-map]))))]
      (cond-> {:db (assoc db ::spec/loading? false
                             ::spec/deployments deployments)}
              (not-empty deployments-resource-ids) (assoc ::cimi-api-fx/search
                                                          [client "deploymentParameters" query-params callback])))))


(defn get-query-params
  [full-text-search page elements-per-page]
  (cond-> {:$first   (inc (* (dec page) elements-per-page))
           :$last    (* page elements-per-page)
           :$orderby "created:desc"}
          (not-empty full-text-search) (assoc :$filter (str "description=='" full-text-search "*'"))))


(reg-event-fx
  ::get-deployments
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/page
                ::spec/elements-per-page] :as db} :db} _]
    {:db                  (assoc db ::spec/loading? true)
     ::cimi-api-fx/search [client "deployments" (get-query-params full-text-search page elements-per-page)
                           #(dispatch [::set-deployments %])]}))


(reg-event-fx
  ::set-page
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/page
                ::spec/elements-per-page] :as db} :db} [_ page]]
    {:db                  (assoc db ::spec/page page)
     ::cimi-api-fx/search [client "deployments" (get-query-params full-text-search page elements-per-page)
                           #(dispatch [::set-deployments %])]}))


(reg-event-db
  ::set-query-params
  (fn [db [_ params-map]]
    (update db ::spec/query-params merge params-map)))
