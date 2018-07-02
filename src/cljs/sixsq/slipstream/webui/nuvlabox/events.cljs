(ns sixsq.slipstream.webui.nuvlabox.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.nuvlabox.effects :as nuvlabox-fx]
    [sixsq.slipstream.webui.nuvlabox.spec :as nuvlabox-spec]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [sixsq.slipstream.webui.utils.response :as response]))


(reg-event-db
  ::set-health-info
  (fn [db [_ state-info]]
    (assoc db
      ::nuvlabox-spec/loading? false
      ::nuvlabox-spec/health-info state-info)))


(reg-event-fx
  ::fetch-health-info
  (fn [{:keys [db]} _]
    (if-let [client (::client-spec/client db)]
      {:db                             (assoc db ::nuvlabox-spec/loading? true)
       ::nuvlabox-fx/fetch-health-info [client #(dispatch [::set-health-info %])]}
      {:db db})))


;; from CIMI

(reg-event-db
  ::set-last
  (fn [db [_ last-value]]
    (update db ::nuvlabox-spec/query-params merge {:$last last-value})))


(reg-event-fx
  ::get-results
  (fn [{{:keys [::nuvlabox-spec/query-params
                ::nuvlabox-spec/state-selector
                ::client-spec/client] :as db} :db} _]
    (let [resource-type :nuvlaboxRecords
          filter (case state-selector
                   "new" "state='new'"
                   "activated" "state='activated'"
                   "quarantined" "state='quarantined'"
                   nil)]
      {:db                  (assoc db ::nuvlabox-spec/loading? true
                                      ::nuvlabox-spec/aggregations nil
                                      ::nuvlabox-spec/collection nil)
       ::cimi-api-fx/search [client
                             resource-type
                             (general-utils/prepare-params (assoc query-params :$filter filter))
                             #(dispatch [::set-results resource-type %])]})))


(reg-event-db
  ::set-results
  (fn [db [_ resource-type listing]]
    (let [error? (instance? js/Error listing)
          entries (get listing (keyword resource-type) [])
          aggregations (get listing :aggregations nil)
          fields (general-utils/merge-keys (conj entries {:id "id"}))]
      (when error?
        (dispatch [::messages-events/add
                   (let [{:keys [status message]} (response/parse-ex-info listing)]
                     {:header  (cond-> (str "failure getting " (name resource-type))
                                       status (str " (" status ")"))
                      :content message
                      :type    :error})]))
      (assoc db ::nuvlabox-spec/aggregations aggregations
                ::nuvlabox-spec/collection (when-not error? listing)
                ::nuvlabox-spec/loading? false
                ::nuvlabox-spec/available-fields fields))))


(reg-event-db
  ::toggle-filter
  (fn [{:keys [::nuvlabox-spec/filter-visible?] :as db} _]
    (assoc db ::nuvlabox-spec/filter-visible? (not filter-visible?))))


(reg-event-db
  ::set-state-selector
  (fn [db [_ state-selector]]
    (assoc db ::nuvlabox-spec/state-selector state-selector)))
