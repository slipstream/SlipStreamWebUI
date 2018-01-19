(ns sixsq.slipstream.webui.cimi-detail.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.cimi-detail.spec :as cimi-detail-spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [taoensso.timbre :as log]))


(reg-event-fx
  ::get
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id]]
    (log/error "DEBUG " client resource-id)
    (when client
      {:db               (assoc db ::cimi-detail-spec/loading? true
                                   ::cimi-detail-spec/resource-id resource-id)
       ::cimi-api-fx/get [client resource-id #(dispatch [::set-resource %])]})))


(reg-event-db
  ::set-resource
  (fn [db [_ resource]]
    (assoc db ::cimi-detail-spec/loading? false
              ::cimi-detail-spec/resource-id (:id resource)
              ::cimi-detail-spec/resource resource)))
