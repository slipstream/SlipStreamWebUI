(ns sixsq.slipstream.webui.data.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]

    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.data.spec :as data-spec]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [sixsq.slipstream.webui.utils.response :as response]))


(reg-event-fx
  ::initialize
  (fn [cofx _]
    (when-let [client (-> cofx :db ::client-spec/client)]
      {::cimi-api-fx/session [client #(dispatch [::set-session %])]})))


(reg-event-db
  ::set-first
  (fn [db [_ first-value]]
    (update db ::data-spec/query-params merge {:$first first-value})))


(reg-event-db
  ::set-last
  (fn [db [_ last-value]]
    (update db ::data-spec/query-params merge {:$last last-value})))


(reg-event-db
  ::set-filter
  (fn [db [_ filter-value]]
    (update db ::data-spec/query-params merge {:$filter filter-value})))


(reg-event-db
  ::set-orderby
  (fn [db [_ orderby-value]]
    (update db ::data-spec/query-params merge {:$orderby orderby-value})))


(reg-event-db
  ::set-select
  (fn [db [_ select-value]]
    (update db ::data-spec/query-params merge {:$select select-value})))

(reg-event-db
  ::set-query-params
  (fn [db [_ params]]
    (update db ::data-spec/query-params merge params)))

(reg-event-db
  ::show-add-modal
  (fn [db _]
    (assoc db ::data-spec/show-add-modal? true)))


(reg-event-db
  ::hide-add-modal
  (fn [db _]
    (assoc db ::data-spec/show-add-modal? false)))


(reg-event-db
  ::set-aggregation
  (fn [db [_ aggregation-value]]
    (update db ::data-spec/query-params merge {:$aggregation aggregation-value})))


(reg-event-db
  ::set-collection-name
  (fn [db [_ collection-name]]
    (-> db
        (assoc ::data-spec/collection-name collection-name)
        (assoc ::data-spec/descriptions-vector []))))


(reg-event-db
  ::set-selected-fields
  (fn [db [_ fields]]
    (assoc db ::data-spec/selected-fields (sort (vec fields)))))


(reg-event-db
  ::remove-field
  (fn [{:keys [::data-spec/selected-fields] :as db} [_ field]]
    (->> ::data-spec/selected-fields
         (remove #{field})
         vec
         sort
         (assoc db ::data-spec/selected-fields))))


(reg-event-fx
  ::get-results
  (fn [{{:keys [::data-spec/collection-name
                ::data-spec/cloud-entry-point
                ::data-spec/query-params
                ::client-spec/client] :as db} :db} _]
    (let [resource-type (-> ::data-spec/cloud-entry-point
                            :collection-key
                            (get ::data-spec/collection-name))]
      {:db                  (assoc db ::data-spec/loading? true
                                      ::data-spec/aggregations nil
                                      ::data-spec/collection nil)
       ::cimi-api-fx/search [client
                             resource-type
                             (general-utils/prepare-params ::data-spec/query-params)
                             #(dispatch [::set-results resource-type %])]})))


(reg-event-fx
  ::create-resource
  (fn [{{:keys [::data-spec/collection-name
                ::data-spec/cloud-entry-point
                ::client-spec/client] :as db} :db} [_ data]]
    (let [resource-type (-> ::data-spec/cloud-entry-point
                            :collection-key
                            (get ::data-spec/collection-name))]
      {::cimi-api-fx/add [client resource-type data
                          #(let [msg-map (if (instance? js/Error %)
                                           (let [{:keys [status message]} (response/parse-ex-info %)]
                                             {:header  (cond-> (str "failure adding " (name resource-type))
                                                               status (str " (" status ")"))
                                              :content message
                                              :type    :error})
                                           (let [{:keys [status message resource-id]} (response/parse %)]
                                             {:header  (cond-> (str "added " resource-id)
                                                               status (str " (" status ")"))
                                              :content message
                                              :type    :success}))]
                             (dispatch [::messages-events/add msg-map]))]})))

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
      (assoc db ::data-spec/aggregations aggregations
                ::data-spec/collection (when-not error? listing)
                ::data-spec/loading? false
                ::data-spec/available-fields fields))))


(reg-event-db
  ::set-cloud-entry-point
  (fn [db [_ {:keys [baseURI] :as cep}]]
    (let [href-map (cimi-utils/collection-href-map cep)
          key-map (cimi-utils/collection-key-map cep)]
      (assoc db ::data-spec/cloud-entry-point {:baseURI         baseURI
                                               :collection-href href-map
                                               :collection-key  key-map}))))


(reg-event-fx
  ::get-cloud-entry-point
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    (when client
      {::cimi-api-fx/cloud-entry-point
       [client (fn [cep]
                 (dispatch [::set-cloud-entry-point cep]))]})))

(reg-event-fx
  ::get-description
  (fn [cofx [_ template]]
    {::cimi-api-fx/get-description [template #(dispatch [::set-description %])]}))

(reg-event-fx
  ::get-templates
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-type]]
    (when client
      {::cimi-api-fx/get-templates [client resource-type
                                    #(doseq [result %]
                                       (dispatch [::get-description result]))]})))

(reg-event-db
  ::set-description
  (fn [db [_ description]]
    (let [description-map {(:id description) description}]
      (update db ::data-spec/descriptions-vector merge description))))


(reg-event-db
  ::toggle-filter
  (fn [{:keys [::data-spec/filter-visible?] :as db} _]
    (assoc db ::data-spec/filter-visible? (not ::data-spec/filter-visible?))))
