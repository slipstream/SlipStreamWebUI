(ns sixsq.slipstream.webui.usage.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<! >! chan timeout]]
    [promesa.core :as p]
    [clojure.string :as str]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn same-date?
  "Returns true if the two dates represent the same time to within 1 minute,
   otherwise false. If either argument is nil, false is returned."
  [d1 d2]
  (boolean (and d1 d2 (zero? (int (time/delta-minutes d1 d2))))))


(defn date-range
  "Provides a tuple that represents the start/end of a range of days relative
   to the current day. The arguments [0 0] would represent the start/end of the
   current day."
  [start end]
  [(time/days-before start) (.endOf (time/days-before end) "day")])


(defn default-date-range
  "Provides the default date range."
  []
  (date-range 30 1))


(defn fetch-metering [resolve client date-after date-before user connector]
  (go
    (let [filter-created-str (str "snapshot-time>'" date-after "' and snapshot-time<'" date-before "'")
          filter-user-str (when user (str "acl/rules/principal='" user "'"))
          filter-connectors (when-not (= connector "all-clouds") (str "connector/href='" connector "'"))
          filter-str (str/join " and " (remove nil? [filter-created-str filter-user-str filter-connectors]))
          request-opts {"$last"        0
                        "$filter"      filter-str
                        "$aggregation" (str "sum:serviceOffer/resource:vcpu, sum:serviceOffer/resource:ram, "
                                            "sum:serviceOffer/resource:disk")}
          response (<! (cimi/search client "meterings" request-opts))]
      (resolve
        [(keyword connector) {:vms  (get response :count 0)
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
