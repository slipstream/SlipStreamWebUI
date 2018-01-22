(ns sixsq.slipstream.webui.dashboard.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.api.runs :as runs]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [clojure.string :as str]
    [taoensso.timbre :as log]))

(reg-fx
  ::get-virtual-machines
  (fn [[client params]]
    (go
      (let [virtual-machines (<! (cimi/search client "virtualMachines" (general-utils/prepare-params params)))]
        (dispatch [:sixsq.slipstream.webui.dashboard.events/set-virtual-machines virtual-machines])))))


;(defn fetch-deployments []
;  (go
;    (let [response (<! (runs/search-runs client/client (get @app-state :request-opts)))
;          item (get-in response [:runs :item] [])
;          deployments-list (if (= (type item) cljs.core/PersistentVector) item [item])]
;      ; workaround gson issue, when one element in item it give element instead of a vector of element
;
;      (if (empty? deployments-list)
;        (do (state-set-deployments (assoc-in response [:runs :item] deployments-list))
;            (state-disable-loading))
;        (-> (p/promise
;              (fn [resolve _] (fetch-active-vms resolve deployments-list)))
;            (p/then (fn [active-vms-per-deployment]
;                      (->> deployments-list
;                           (map #(assoc % :active-vm
;                                          (get active-vms-per-deployment (keyword (str "run/" (:uuid %))) 0)))
;                           (assoc-in response [:runs :item])
;                           state-set-deployments)
;                      (state-disable-loading))))
;        ))))


(reg-fx
  ::get-deployments
  (fn [[client params]]
    (go
      (let [response (<! (runs/search-runs client params))
            item (get-in response [:runs :item] [])         ; workaround gson issue, when one element in item it give element instead of a vector of element
            deployments-list (if (= (type item) cljs.core/PersistentVector) item [item])
            deployments-uuid (map #(str "deployment/href=\"run/" (:uuid %) "\"") deployments-list)
            filter-str (str/join " or " deployments-uuid)
            active-vms (<! (cimi/search client "virtualMachines" {"$last"        0
                                                                  "$aggregation" "terms:deployment/href"
                                                                  "$filter"      filter-str}))
            active-vms-per-deployment (->> (get-in active-vms [:aggregations :terms:deployment/href :buckets] [])
                                           (map #(vector (keyword (:key %)) (:doc_count %)))
                                           (into {}))
            deployments-with-vms (->> deployments-list
                                      (map #(assoc % :activeVm
                                                     (get active-vms-per-deployment
                                                          (keyword (str "run/" (:uuid %))) 0)))
                                      (assoc-in response [:runs :item]))]
        (dispatch [:sixsq.slipstream.webui.dashboard.events/set-deployments deployments-with-vms])))))
