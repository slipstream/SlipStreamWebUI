(ns sixsq.slipstream.webui.metrics.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.metrics.spec :as metrics-spec]))


(reg-event-db
  ::set-metrics
  (fn [db [_ metrics]]
    (assoc db
      ::metrics-spec/loading? false
      ::metrics-spec/raw-metrics metrics)))


(reg-event-fx
  ::fetch-metrics
  (fn [{:keys [db]} _]
    (if-let [client (::client-spec/client db)]
      {:db                   (assoc db ::metrics-spec/loading? true)
       ::cimi-api-fx/metrics [client #(dispatch [::set-metrics %])]}
      {:db db})))
