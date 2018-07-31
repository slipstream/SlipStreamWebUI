(ns sixsq.slipstream.webui.usage.utils
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<! >! chan timeout]]
    [clojure.string :as str]
    [promesa.core :as p]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.utils.time :as time]))


(def vms-unit "VMs [h]")
(def cpus-unit "CPUs [h]")
(def ram-unit "RAM [GB·h]")
(def disk-unit "DISK [GB·h]")
(def price-unit "PRICE [€]")

(def all-credentials "all-credentials")

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


(defn to-hour [v]
  (/ v 60))


(defn to-GB-from-MB [v]
  (/ v 1024))


(defn fetch-metering [resolve client date-after date-before user credential]
  (go
    (let [filter-created-str (str "snapshot-time>'" date-after "' and snapshot-time<'" date-before "'")
          filter-user-str (when user (str "acl/rules/principal='" user "'"))
          filter-credentials (when-not (= credential all-credentials) (str "credentials/href='" credential "'"))
          filter-str (str/join " and " (remove nil? [filter-created-str filter-user-str filter-credentials]))
          request-opts {"$last"        0
                        "$filter"      filter-str
                        "$aggregation" (str "sum:serviceOffer/resource:vcpu, sum:serviceOffer/resource:ram, "
                                            "sum:serviceOffer/resource:disk, sum:price")}
          response (<! (cimi/search client "meterings" request-opts))]
      (resolve
        [credential {:vms   {:unit  vms-unit
                             :value (-> response (get :count 0) to-hour)}
                     :cpus  {:unit  cpus-unit
                             :value (-> response
                                        (get-in [:aggregations :sum:serviceOffer/resource:vcpu :value] 0)
                                        to-hour)}
                     :ram   {:unit  ram-unit
                             :value (-> response
                                        (get-in [:aggregations :sum:serviceOffer/resource:ram :value] 0)
                                        to-GB-from-MB
                                        to-hour)}
                     :disk  {:unit  disk-unit
                             :value (-> response
                                        (get-in [:aggregations :sum:serviceOffer/resource:disk :value] 0)
                                        to-GB-from-MB
                                        to-hour)}
                     :price {:unit  price-unit
                             :value (-> response (get-in [:aggregations :sum:price :value] 0)
                                        to-hour)}}]))))


(defn fetch-meterings [client
                       date-after
                       date-before
                       user
                       credentials
                       callback]
  (let [p (p/all (map #(p/promise (fn [resolve _]
                                    (fetch-metering resolve client date-after date-before user %))) credentials))]
    (p/then p #(->> % (into {}) callback))))
