(ns cubic.deployment.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]

    [cubic.utils.general :as general-utils]

    [cubic.cimi-api.effects :as cimi-api-fx]
    [cubic.client.spec :as client-spec]
    [cubic.deployment.spec :as deployment-spec]
    [cubic.deployment.effects :as deployment-fx]))


(reg-event-db
  ::set-deployments
  (fn [db [_ deployments]]
    (assoc db ::deployment-spec/loading? false
              ::deployment-spec/deployments deployments)))


(reg-event-fx
  ::get-deployments
  (fn [{{:keys [::client-spec/client ::deployment-spec/query-params] :as db} :db} _]
    {:db                             (assoc db ::deployment-spec/loading? true)
     ::deployment-fx/get-deployments [client query-params]}))


(reg-event-db
  ::set-query-params
  (fn [db [_ params-map]]
    (update db ::deployment-spec/query-params merge params-map)))
