(ns sixsq.slipstream.webui.data.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.data.spec :as spec]
    [sixsq.slipstream.webui.data.spec :as spec]
    [sixsq.slipstream.webui.data.utils :as utils]
    [sixsq.slipstream.webui.history.events :as history-evts]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [sixsq.slipstream.webui.utils.response :as response]

    [taoensso.timbre :as log]))




(reg-event-db
  ::set-time-period
  (fn [db [_ time-period]]
    (assoc db ::spec/time-period time-period
              ::spec/time-period-filter (utils/create-time-period-filter time-period))))


(reg-event-db
  ::set-service-offers
  (fn [db [_ service-offers]]
    (assoc db ::spec/service-offers service-offers)))


(reg-event-db
  ::set-credentials
  (fn [db [_ {:keys [credentials]}]]
    (assoc db ::spec/credentials credentials
              ::spec/cloud-filter (utils/create-cloud-filter credentials))))


;;(reg-event-fx
;;  ::get-service-offers
;;  (fn [{{:keys [::client-spec/client
;;                ::spec/time-period
;;                ::spec/credentials] :as db} :db} _]
;;    (when client
;;      (let [filter (filter-service-offer time-period credentials)]
;;        (cond-> {:db (assoc db ::spec/service-offers nil)}
;;                (not-empty credentials) (assoc ::cimi-api-fx/search
;;                                               [client "serviceOffers" {:$filter filter}
;;                                                #(dispatch [::set-service-offers %])]))))))


(reg-event-db
  ::set-content-types
  (fn [db [_ content-types-response]]
    (let [buckets (get-in content-types-response [:aggregations :terms:data:contentType :buckets])]
      (assoc db ::spec/content-types buckets))))


(reg-event-fx
  ::get-content-types
  (fn [{{:keys [::client-spec/client
                ::spec/time-period-filter
                ::spec/cloud-filter
                ::spec/gnss-filter
                ::spec/credentials] :as db} :db} _]
    (when client
      (let [filter (utils/join-filters time-period-filter cloud-filter gnss-filter)]
        (cond-> {:db (assoc db ::spec/content-types nil)}
                (not-empty credentials) (assoc ::cimi-api-fx/search
                                               [client "serviceOffers" {:$filter      filter
                                                                        :$last        0
                                                                        :$aggregation "terms:data:contentType"}
                                                #(dispatch [::set-content-types %])]))))))


(reg-event-fx
  ::get-credentials
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    (when client
      {:db                  (assoc db ::spec/credentials nil)
       ::cimi-api-fx/search [client "credentials" {:$filter "type^='cloud-cred'"
                                                   :$select "id, name, connector"}
                             #(dispatch [::set-credentials %])]})))


(reg-event-db
  ::set-applications
  (fn [db [_ applications]]
    (assoc db ::spec/applications (:modules applications)
              ::spec/loading-applications? false)))


(reg-event-fx
  ::open-application-select-modal
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ content-type]]
    {:db                  (assoc db ::spec/application-select-visible? true
                                    ::spec/loading-applications? true
                                    ::spec/content-type-filter (str "data:contentType='" content-type "'"))
     ::cimi-api-fx/search [client "modules" {:$filter (str "dataAcceptContentTypes='" content-type "'")}
                           #(dispatch [::set-applications %])]
     }))


(reg-event-db
  ::close-application-select-modal
  (fn [db _]
    (assoc db ::spec/applications nil
              ::spec/application-select-visible? false)))

