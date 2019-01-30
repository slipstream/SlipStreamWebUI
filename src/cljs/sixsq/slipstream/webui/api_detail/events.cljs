(ns sixsq.slipstream.webui.api-detail.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.api-detail.spec :as api-detail-spec]
    [sixsq.slipstream.webui.api.spec :as api-spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.response :as response]
    [taoensso.timbre :as log]))


(reg-event-fx
  ::get
  (fn [{{:keys                                  [::client-spec/client
                ::api-spec/collection-name] :as db} :db} [_ resource-id]]
    (when client
      {:db               (assoc db ::api-detail-spec/loading? true
                                   ::api-detail-spec/resource-id resource-id)
       ::cimi-api-fx/get [client resource-id #(if (instance? js/Error %)
                                                (let [{:keys [status message]} (response/parse-ex-info %)]
                                                  (dispatch [::messages-events/add
                                                             {:header  (cond-> (str "error getting " resource-id)
                                                                               status (str " (" status ")"))
                                                              :content message
                                                              :type    :error}])
                                                  (dispatch [::history-events/navigate
                                                             (str "api/" ::api-spec/collection-name)]))
                                                (dispatch [::set-resource %]))]})))


(reg-event-fx
  ::set-resource
  (fn [{{:keys                                    [::client-spec/client
                ::api-spec/collection-name
                ::api-spec/cloud-entry-point] :as db} :db} [_ {:keys [operations] :as resource}]]
    (let [tpl-resources-key (-> ::api-spec/cloud-entry-point
                                (get :collection-key "")
                                (get (str ::api-spec/collection-name "-template") ""))
          tpl-resource-key (->> tpl-resources-key
                                name
                                drop-last
                                (str/join "")
                                keyword)
          tpl-id (-> resource
                     (get tpl-resource-key)
                     :href)
          describe-operation (->> operations
                                  (filter #(= (-> % :rel general/operation-name) "describe"))
                                  first
                                  :rel)]
      (log/info (:id resource))
      (cond-> {:db (assoc db ::api-detail-spec/loading? false
                             ::api-detail-spec/resource-id (:id resource)
                             ::api-detail-spec/resource resource
                             ::api-detail-spec/description nil)}
              (and tpl-id describe-operation) (assoc ::cimi-api-fx/operation
                                                     [client tpl-id describe-operation
                                                      #(dispatch [::set-description
                                                                  {:href                  tpl-id
                                                                   :template-resource-key tpl-resource-key
                                                                   :params-desc           %}])])))))

(reg-event-db
  ::set-description
  (fn [db [_ description]]
    (assoc db ::api-detail-spec/description description)))


(reg-event-fx
  ::delete
  (fn [{{:keys                                  [::client-spec/client
                ::api-spec/collection-name] :as db} :db} [_ resource-id]]
    (when client
      {::cimi-api-fx/delete [client resource-id
                             #(if (instance? js/Error %)
                                (let [{:keys [status message]} (response/parse-ex-info %)]
                                  (dispatch [::messages-events/add
                                             {:header  (cond-> (str "error deleting " resource-id)
                                                               status (str " (" status ")"))
                                              :content message
                                              :type    :error}]))
                                (let [{:keys [status message]} (response/parse %)]
                                  (dispatch [::messages-events/add
                                             {:header  (cond-> (str "deleted " resource-id)
                                                               status (str " (" status ")"))
                                              :content message
                                              :type    :success}])
                                  (dispatch [::history-events/navigate (str "api/" ::api-spec/collection-name)])))]
       })))


(reg-event-fx
  ::edit
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id data]]
    (when client
      {::cimi-api-fx/edit [client resource-id data
                           #(if (instance? js/Error %)
                              (let [{:keys [status message]} (response/parse-ex-info %)]
                                (dispatch [::messages-events/add
                                           {:header  (cond-> (str "error editing " resource-id)
                                                             status (str " (" status ")"))
                                            :content message
                                            :type    :error}]))
                              (dispatch [::set-resource %]))]})))


(reg-event-fx
  ::operation
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id operation]]
    {::cimi-api-fx/operation [client resource-id operation
                              #(let [op (second (re-matches #"(?:.*/)?(.*)" operation))]
                                 (if (instance? js/Error %)
                                   (let [{:keys [status message]} (response/parse-ex-info %)]
                                     (dispatch [::messages-events/add
                                                {:header  (cond-> (str "error executing operation " op)
                                                                  status (str " (" status ")"))
                                                 :content message
                                                 :type    :error}]))
                                   (let [{:keys [status message]} (response/parse %)]
                                     (dispatch [::messages-events/add
                                                {:header  (cond-> (str "success executing operation " op)
                                                                  status (str " (" status ")"))
                                                 :content message
                                                 :type    :success}]))))]}))
