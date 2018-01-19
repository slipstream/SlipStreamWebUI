(ns sixsq.slipstream.webui.dashboard.events
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]

    [sixsq.slipstream.webui.utils.general :as general-utils]

    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.dashboard.spec :as dashboard-spec]
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
