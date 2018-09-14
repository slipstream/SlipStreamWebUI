(ns sixsq.slipstream.webui.deployment.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.deployment.effects :as deployment-fx]
    [sixsq.slipstream.webui.deployment.spec :as deployment-spec]
    [sixsq.slipstream.webui.utils.general :as general-utils]))


(reg-event-db
  ::set-deployments
  (fn [db [_ deployments]]
    (assoc db ::deployment-spec/loading? false
              ::deployment-spec/deployments deployments)))


(reg-event-fx
  ::get-deployments
  (fn [{{:keys [::client-spec/client ::deployment-spec/query-params] :as db} :db} _]
    {:db                             (assoc db ::deployment-spec/loading? true)
     ::deployment-fx/get-deployments [client (general-utils/prepare-params query-params)]}))


(reg-event-db
  ::set-query-params
  (fn [db [_ params-map]]
    (update db ::deployment-spec/query-params merge params-map)))
