(ns sixsq.slipstream.webui.usage.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<! >! chan timeout]]
    [promesa.core :as p]
    [clojure.string :as str]
    [sixsq.slipstream.client.api.cimi :as cimi]))

(defn fetch-metering [resolve client date-after date-before user connector]
  (go
    (let [filter-created-str (str "created>'" date-after "' and created<'" date-before "'")
          filter-user-str (when user (str "acl/rules/principal='" user "'"))
          filter-connectors (when-not (= connector "all-clouds") (str "connector/href='" connector "'"))
          filter-str (str/join " and " (remove nil? [filter-created-str filter-user-str filter-connectors]))
          request-opts {"$last"        0
                        "$filter"      filter-str
                        "$aggregation" (str "cardinality:instanceID, sum:serviceOffer/resource:vcpu, "
                                            "sum:serviceOffer/resource:ram, sum:serviceOffer/resource:disk")}
          response (<! (cimi/search client "meterings" request-opts))]
      (resolve
        [(keyword connector) {:vms  (get-in response [:aggregations :cardinality:instanceID :value] 0)
                              :vcpu (get-in response [:aggregations :sum:serviceOffer/resource:vcpu :value] 0)
                              :ram  (get-in response [:aggregations :sum:serviceOffer/resource:ram :value] 0)
                              :disk (get-in response [:aggregations :sum:serviceOffer/resource:disk :value] 0)}]))))

(defn fetch-meterings [client
                       date-after
                       date-before
                       user
                       connectors
                       callback]
  (let [p (p/all (map #(p/promise (fn [resolve _]
                                    (fetch-metering resolve client date-after date-before user %))) connectors))]
    (p/then p #(->> % (into {}) callback))))
