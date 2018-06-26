(ns sixsq.slipstream.webui.nuvlabox.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.nuvlabox.effects :as nuvlabox-fx]
    [sixsq.slipstream.webui.nuvlabox.spec :as nuvlabox-spec]))


(reg-event-db
  ::set-state-info
  (fn [db [_ state-info]]
    (assoc db
      ::nuvlabox-spec/loading? false
      ::nuvlabox-spec/state-info state-info)))


(reg-event-fx
  ::fetch-state-info
  (fn [{:keys [db]} _]
    (if-let [client (::client-spec/client db)]
      {:db                            (assoc db ::nuvlabox-spec/loading? true)
       ::nuvlabox-fx/fetch-state-info [client #(dispatch [::set-state-info %])]}
      {:db db})))


(reg-event-db
  ::set-detail
  (fn [db [_ detail]]
    (assoc db
      ::nuvlabox-spec/detail-loading? false
      ::nuvlabox-spec/detail detail)))


(reg-event-db
  ::set-mac
  (fn [db [_ mac]]
    (assoc db ::nuvlabox-spec/mac mac)))


(reg-event-fx
  ::fetch-detail
  (fn [{{:keys [::nuvlabox-spec/mac] :as db} :db} _]
    (if-let [client (::client-spec/client db)]
      {:db                        (assoc db ::nuvlabox-spec/detail-loading? true)
       ::nuvlabox-fx/fetch-detail [client mac #(dispatch [::set-detail %])]}
      {:db db})))
