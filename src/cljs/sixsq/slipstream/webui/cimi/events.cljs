(ns sixsq.slipstream.webui.cimi.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.i18n.utils :as utils]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [taoensso.timbre :as log]))


(reg-event-fx
  ::initialize
  (fn [cofx _]
    (when-let [client (-> cofx :db ::client-spec/client)]
      {::cimi-api-fx/session [client #(dispatch [::set-session %])]})))


(reg-event-db
  ::set-first
  (fn [db [_ first-value]]
    (update db ::cimi-spec/query-params merge {:$first first-value})))


(reg-event-db
  ::set-last
  (fn [db [_ last-value]]
    (update db ::cimi-spec/query-params merge {:$last last-value})))


(reg-event-db
  ::set-filter
  (fn [db [_ filter-value]]
    (update db ::cimi-spec/query-params merge {:$filter filter-value})))


(reg-event-db
  ::set-orderby
  (fn [db [_ orderby-value]]
    (update db ::cimi-spec/query-params merge {:$orderby orderby-value})))


(reg-event-db
  ::set-select
  (fn [db [_ select-value]]
    (update db ::cimi-spec/query-params merge {:$select select-value})))


(reg-event-db
  ::show-add-modal
  (fn [db _]
    (assoc db ::cimi-spec/show-add-modal? true)))


(reg-event-db
  ::hide-add-modal
  (fn [db _]
    (assoc db ::cimi-spec/show-add-modal? false)))


(reg-event-db
  ::set-aggregation
  (fn [db [_ aggregation-value]]
    (update db ::cimi-spec/query-params merge {:$aggregation aggregation-value})))


(reg-event-db
  ::set-collection-name
  (fn [db [_ collection-name]]
    (assoc db ::cimi-spec/collection-name collection-name)))


(reg-event-db
  ::set-selected-fields
  (fn [db [_ fields]]
    (assoc db ::cimi-spec/selected-fields (sort (vec fields)))))


;(reg-event-fx
;  ::get-results
;  (fn [db [_ collection-name]]
;    {::cimi-api-fx/search #(dispatch [::set-results %])}))


(reg-event-fx
  ::get-results
  (fn [{{:keys [::cimi-spec/collection-name
                ::cimi-spec/cloud-entry-point
                ::cimi-spec/query-params
                ::client-spec/client] :as db} :db} _]
    (let [resource-type (-> cloud-entry-point
                            :collection-key
                            (get collection-name))]
      {:db                  (assoc db ::cimi-spec/loading? true
                                      ::cimi-spec/aggregations nil
                                      ::cimi-spec/collection nil)
       ::cimi-api-fx/search [client
                             resource-type
                             (general-utils/prepare-params query-params)
                             #(dispatch [::set-results resource-type %])]})))


(reg-event-fx
  ::create-resource-no-tpl
  (fn [{{:keys [::cimi-spec/collection-name
                ::cimi-spec/cloud-entry-point
                ::client-spec/client] :as db} :db} [_ data]]
    (let [resource-type (-> cloud-entry-point
                            :collection-key
                            (get collection-name))]
      {::cimi-api-fx/add [client resource-type data
                          #(if (instance? js/Error %)
                             (let [error (->> % ex-data)]
                               (dispatch [::main-events/set-message {:header  "Failure"
                                                                     :content (:body error)
                                                                     :type    :error}]))
                             (dispatch [::main-events/set-message {:header  "Success"
                                                                   :content (:message %)
                                                                   :type    :success}]))]})))

(reg-event-db
  ::set-results
  (fn [db [_ resource-type listing]]
    (let [entries (get listing (keyword resource-type) [])
          aggregations (:aggregations listing)
          fields (general-utils/merge-keys (conj entries {:id "id"}))]
      (assoc db ::cimi-spec/aggregations aggregations
                ::cimi-spec/collection listing
                ::cimi-spec/loading? false
                ::cimi-spec/available-fields fields))))


(reg-event-db
  ::set-cloud-entry-point
  (fn [db [_ {:keys [baseURI] :as cep}]]
    (let [href-map (cimi-utils/collection-href-map cep)
          key-map (cimi-utils/collection-key-map cep)]
      (assoc db ::cimi-spec/cloud-entry-point {:baseURI         baseURI
                                               :collection-href href-map
                                               :collection-key  key-map}))))


(reg-event-fx
  ::get-cloud-entry-point
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    (when client
      {::cimi-api-fx/cloud-entry-point
       [client (fn [cep]
                 (dispatch [::set-cloud-entry-point cep]))]})))

