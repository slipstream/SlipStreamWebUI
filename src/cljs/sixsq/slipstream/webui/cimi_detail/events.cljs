(ns sixsq.slipstream.webui.cimi-detail.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.cimi-detail.spec :as cimi-detail-spec]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]
    [sixsq.slipstream.client.impl.utils.json :as json]
    [sixsq.slipstream.webui.history.events :as history-events]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]
    [clojure.string :as str]))


(reg-event-fx
  ::get
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id]]
    (when client
      {:db               (assoc db ::cimi-detail-spec/loading? true
                                   ::cimi-detail-spec/resource-id resource-id)
       ::cimi-api-fx/get [client resource-id #(dispatch [::set-resource %])]})))



(defn operation-name [op-uri]
  (second (re-matches #"^(?:.*/)?(.+)$" op-uri)))

(reg-event-fx
  ::set-resource
  (fn [{{:keys [::client-spec/client
                ::cimi-spec/collection-name
                ::cimi-spec/cloud-entry-point] :as db} :db} [_ {:keys [operations] :as resource}]]
    (let [tpl-resources-key (-> cloud-entry-point
                                (get :collection-key "")
                                (get (str collection-name "-template") ""))
          tpl-resource-key (->> tpl-resources-key
                                name
                                drop-last
                                (str/join "")
                                keyword)
          tpl-id (-> resource
                     (get tpl-resource-key)
                     :href)
          describe-operation (->> operations
                                  (filter #(= (-> % :rel operation-name) "describe"))
                                  first
                                  :rel)]
      (cond-> {:db (assoc db ::cimi-detail-spec/loading? false
                             ::cimi-detail-spec/resource-id (:id resource)
                             ::cimi-detail-spec/resource resource
                             ::cimi-detail-spec/description nil)}
              (and tpl-id describe-operation) (assoc ::cimi-api-fx/operation
                                                     [client tpl-id describe-operation
                                                      #(dispatch [::set-description %])])))))

(reg-event-db
  ::set-description
  (fn [db [_ description]]
    (assoc db ::cimi-detail-spec/description description)))

(reg-event-fx
  ::delete
  (fn [{{:keys [::client-spec/client
                ::cimi-spec/collection-name
                ] :as db} :db} [_ resource-id]]
    (when client
      {::cimi-api-fx/delete [client resource-id
                             #(if (instance? js/Error %)
                                (let [error (->> % ex-data)]
                                  (dispatch [::main-events/set-message {:header  "Failure"
                                                                        :content (:body error)
                                                                        :type    :error}]))
                                (do
                                  (dispatch [::main-events/set-message {:header  "Success"
                                                                        :content (:message %)
                                                                        :type    :success}])
                                  (dispatch [::history-events/navigate (str "cimi/" collection-name)])))]
       })))

(reg-event-fx
  ::edit
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id data]]
    (when client
      {::cimi-api-fx/edit [client resource-id data #(if (instance? js/Error %)
                                                      (let [error (->> % ex-data)]
                                                        (dispatch [::main-events/set-message {:header  "Failure"
                                                                                              :content (:body error)
                                                                                              :type    :error}]))
                                                      (dispatch [::set-resource (:body %)]))]
       })))

(reg-event-fx
  ::operation
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ resource-id operation]]
    {::cimi-api-fx/operation [client resource-id operation #(if (instance? js/Error %)
                                                              (let [error (->> % ex-data)]
                                                                (dispatch [::main-events/set-message
                                                                           {:header  "Failure"
                                                                            :content (:body error)
                                                                            :type    :error}]))
                                                              (dispatch [::main-events/set-message
                                                                         {:header  "Success"
                                                                          :content (with-out-str (cljs.pprint/pprint %))
                                                                          :type    :success}]))]}))
