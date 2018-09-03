(ns sixsq.slipstream.webui.quota.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.quota.spec :as quota-spec]
    [taoensso.timbre :as log]))

(reg-event-fx
  ::get-quotas
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    {::cimi-api-fx/search [client
                           :quotas
                           {:$filter "resource='VirtualMachine'"}
                           #(dispatch [::set-credentials-quotas-map %])]}))

(reg-event-db
  ::set-credentials-quotas-map
  (fn [db [_ response]]
    (let [quotas (get response :quotas [])]
      (-> db
          (assoc ::quota-spec/credentials-quotas-map (group-by :selection quotas))
          (assoc ::quota-spec/loading-quotas? false)))))
