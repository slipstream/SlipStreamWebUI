(ns sixsq.slipstream.webui.dashboard.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-event-db reg-event-fx dispatch debug]]

    [sixsq.slipstream.webui.utils.general :as general-utils]

    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.dashboard.spec :as dashboard-spec]
    [sixsq.slipstream.webui.dashboard.effects :as dashboard-fx]
    [sixsq.slipstream.webui.main.effects :as main-fx]
    [taoensso.timbre :as log]))


(def stat-info {:count                          {:label "VMs", :order 0}
                :cardinality:connector/href     {:label "clouds", :order 1}
                :sum:serviceOffer/resource:vcpu {:label "vCPU", :order 2}
                :sum:serviceOffer/resource:ram  {:label "RAM (MB)", :order 3}
                :sum:serviceOffer/resource:disk {:label "Disk (GB)", :order 4}})


(reg-event-db
  ::set-statistics
  (fn [db [_ {:keys [_ count aggregations]}]]
    (let [stats (->> aggregations
                     (merge {:count {:value count}})
                     (map (fn [[k v]] (merge v (stat-info k)))))]
      (assoc db ::dashboard-spec/loading? false
                ::dashboard-spec/statistics stats))))


(reg-event-fx
  ::get-statistics
  (fn [{{:keys [::client-spec/client] :as db} :db} _]
    (let [collection-name "virtualMachines"
          params {:$first       1
                  :$last        0
                  :$filter      "state='Running'"
                  :$orderby     nil
                  :$aggregation "sum:serviceOffer/resource:disk,sum:serviceOffer/resource:ram,sum:serviceOffer/resource:vcpu,cardinality:connector/href"
                  :$select      nil}]
      {:db                  (assoc db ::dashboard-spec/loading? true)
       ::cimi-api-fx/search [client
                             collection-name
                             (general-utils/prepare-params params)
                             #(dispatch [::set-statistics %])]})))

(reg-event-fx
  ::set-selected-tab
  [debug]
  (fn [{:keys [db]} [_ tab-index]]
    {:db                       (-> db
                                   (assoc ::dashboard-spec/selected-tab tab-index)
                                   (assoc ::dashboard-spec/page 1))
     ::main-fx/action-interval [{:action    :start
                                 :id        :dashboard-tab
                                 :frequency 10000
                                 :event     [(case tab-index
                                               0 ::get-deployments
                                               1 ::get-virtual-machines)]}]}))

(reg-event-db                                               ; TODO set loading and fetch records
  ::set-filtered-cloud
  [debug]
  (fn [db [_ cloud]]
    (assoc db ::dashboard-spec/filtered-cloud cloud)))

(defn page-count [record-displayed element-count]
  (cond-> element-count
          true (quot record-displayed)
          (pos? (mod element-count record-displayed)) inc))

(reg-event-db
  ::set-virtual-machines
  [debug]
  (fn [{:keys [::dashboard-spec/records-displayed] :as db} [_ virtual-machines]]
    (let [total-pages (page-count records-displayed (:count virtual-machines))
          new-db (-> db
                     (assoc ::dashboard-spec/virtual-machines virtual-machines)
                     (assoc ::dashboard-spec/total-pages total-pages))]
      (cond-> new-db
              (> (:page db) total-pages) (assoc ::dashboard-spec/page total-pages)))))

(defn fetch-vms-cofx [{:keys [::client-spec/client
                              ::dashboard-spec/filtered-cloud
                              ::dashboard-spec/page
                              ::dashboard-spec/records-displayed] :as db}]
  (let [last (* page records-displayed)
        first (+ (- last records-displayed) 1)]
    {::dashboard-fx/get-virtual-machines [client {:$filter (when filtered-cloud
                                                             (str "connector/href=\"connector/" filtered-cloud "\""))
                                                  :$order  "created:desc"
                                                  :$first  first
                                                  :$last   last}]}))

(defn fetch-deployments-cofx [{:keys [::client-spec/client
                                      ::dashboard-spec/filtered-cloud
                                      ::dashboard-spec/page
                                      ::dashboard-spec/records-displayed
                                      ::dashboard-spec/active-deployments-only] :as db}]
  (let [offset (* (dec page) records-displayed)]
    {::dashboard-fx/get-deployments [client {:offset     offset
                                             :limit      records-displayed
                                             :cloud      (or filtered-cloud "")
                                             :activeOnly (if active-deployments-only 1 0)}]}))

(reg-event-db
  ::set-deployments
  [debug]
  (fn [{:keys [::dashboard-spec/records-displayed] :as db}  [_ deployments]]
    (let [deployments-count (get-in deployments [:runs :totalCount] 0)
          total-pages (page-count records-displayed deployments-count)
          new-db (-> db
                     (assoc ::dashboard-spec/deployments deployments)
                     (assoc ::dashboard-spec/total-pages total-pages))]
      (cond-> new-db
              (> (:page db) total-pages) (assoc ::dashboard-spec/page total-pages)))))

(reg-event-fx
  ::get-virtual-machines
  (fn [{:keys [db]} _]
    (fetch-vms-cofx db)))

(reg-event-fx
  ::get-deployments
  (fn [{:keys [db]} _]
    (fetch-deployments-cofx db)))

(reg-event-fx
  ::set-page
  (fn [{{:keys [::dashboard-spec/selected-tab] :as db} :db} [_ page]]
    (let [db (assoc db ::dashboard-spec/page page)]
      (merge ((case selected-tab
                0 fetch-deployments-cofx
                1 fetch-vms-cofx) db) {:db db}))))

(reg-event-fx
  ::active-deployments-only
  [debug]
  (fn [{{:keys [::dashboard-spec/selected-tab] :as db} :db} [_ v]]
    (let [db (assoc db ::dashboard-spec/active-deployments-only v)]
      (merge ((case selected-tab
                0 fetch-deployments-cofx
                1 fetch-vms-cofx) db) {:db db}))))
