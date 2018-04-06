(ns sixsq.slipstream.webui.deployment.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]
    [taoensso.timbre :as log]

    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.deployment.effects :as deployment-fx]
    [sixsq.slipstream.webui.deployment.spec :as deployment-spec]
    [sixsq.slipstream.webui.utils.general :as general-utils]))


(reg-event-db
  ::set-deployments
  (fn [db [_ deployments]]
    (assoc db ::deployment-spec/loading? false
              ::deployment-spec/deployments deployments)))


(reg-event-db
  ::toggle-filter
  (fn [{:keys [::deployment-spec/filter-visible?] :as db} _]
    (assoc db ::deployment-spec/filter-visible? (not filter-visible?))))


(reg-event-fx
  ::get-deployments
  (fn [{{:keys [::client-spec/client ::deployment-spec/query-params] :as db} :db} _]
    {:db                             (assoc db ::deployment-spec/loading? true)
     ::deployment-fx/get-deployments [client (general-utils/prepare-params query-params)]}))


(reg-event-db
  ::set-query-params
  (fn [db [_ params-map]]
    (update db ::deployment-spec/query-params merge params-map)))


(reg-event-db
  ::set-deployment-target
  (fn [db [_ deployment-target]]
    (assoc db ::deployment-spec/deployment-target deployment-target)))


(reg-event-db
  ::clear-deployment-target
  (fn [db _]
    (assoc db ::deployment-spec/deployment-target nil)))


(reg-event-db
  ::set-user-connectors
  (fn [db [_ connectors]]
    (assoc db ::deployment-spec/user-connectors connectors
              ::deployment-spec/user-connectors-loading? false)))


(reg-event-db
  ::clear-user-connectors
  (fn [db _]
    (assoc db ::deployment-spec/user-connectors nil
              ::deployment-spec/user-connectors-loading? false)))


;; FIXME: Move to deployment utilities.
(defn credentials->connectors
  [credentials]
  (->> credentials
       (map :connector)
       (map :href)
       (map general-utils/parse-resource-path)
       (map second)
       sort
       vec))


(reg-event-fx
  ::get-user-connectors
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    {:db                  (assoc db ::deployment-spec/user-connectors-loading? true)
     ::cimi-api-fx/search [client "credentials" {}
                           (fn [{:keys [credentials]}]
                             (let [connectors (credentials->connectors credentials)]
                               (log/error (with-out-str (cljs.pprint/pprint connectors)))
                               (dispatch [::set-user-connectors connectors])))]}))


(reg-event-db
  ::set-place-and-rank
  (fn [db [_ place-and-rank]]
    (assoc db ::deployment-spec/place-and-rank place-and-rank
              ::deployment-spec/place-and-rank-loading? false)))


(reg-event-db
  ::clear-place-and-rank
  (fn [db _]
    (assoc db ::deployment-spec/place-and-rank nil
              ::deployment-spec/place-and-rank-loading? false)))


(reg-event-fx
  ::place-and-rank
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ target-module user-connectors]]
    {:db                          (assoc db ::deployment-spec/place-and-rank-loading? true)
     ::cimi-api-fx/place-and-rank [client (str "module/" target-module) user-connectors
                                   (fn [place-and-rank]
                                     (log/error (with-out-str (cljs.pprint/pprint place-and-rank)))
                                     (dispatch [::set-place-and-rank place-and-rank]))]}))
