(ns sixsq.slipstream.webui.dashboard.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-event-db reg-event-fx dispatch debug]]

    [sixsq.slipstream.webui.utils.general :as general-utils]

    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.dashboard.spec :as dashboard-spec]
    [sixsq.slipstream.webui.dashboard.effects :as dashboard-fx]
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

(reg-event-db
  ::set-selected-tab
  (fn [db [_ tab-index]]
    (assoc db ::dashboard-spec/selected-tab tab-index)))

(reg-event-db                                               ; TODO set loading and fetch records
  ::set-filtered-cloud
  [debug]
  (fn [db [_ cloud]]
    (assoc db ::dashboard-spec/filtered-cloud cloud)))

(defn page-count [record-displayed vms-count]
  (let [full-page-number (quot vms-count record-displayed)
        additionnal-page (if (pos? (mod vms-count record-displayed)) 1 0)]
    (+ full-page-number additionnal-page)))

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
    {::dashboard-fx/get-virtual-machines [client {:$filter filtered-cloud
                                                  :$order  "created:desc"
                                                  :$first  first
                                                  :$last   last}]}))

(reg-event-fx
  ::get-virtual-machines
  (fn [{:keys [db]} _]
    (fetch-vms-cofx db)))

(reg-event-fx
  ::set-page
  (fn [{{:keys [::client-spec/client
                ::dashboard-spec/filtered-cloud
                ::dashboard-spec/page
                ::dashboard-spec/records-displayed] :as db} :db} [_ page]]
    (let [db (assoc db ::dashboard-spec/page page)]
      (merge (fetch-vms-cofx db) {:db db}))))
