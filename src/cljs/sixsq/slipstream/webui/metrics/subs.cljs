(ns sixsq.slipstream.webui.metrics.subs
  (:require
    [re-frame.core :refer [reg-sub subscribe]]
    [sixsq.slipstream.webui.metrics.spec :as metrics-spec]
    [sixsq.slipstream.webui.metrics.utils :as metrics-utils]))


(reg-sub
  ::loading?
  (fn [db]
    (::metrics-spec/loading? db)))


(reg-sub
  ::raw-metrics
  (fn [db]
    (::metrics-spec/raw-metrics db)))


(reg-sub
  ::jvm-threads
  (fn [query-v _]
    (subscribe [::raw-metrics]))
  (fn [raw-metrics query-v _]
    (metrics-utils/extract-thread-metrics raw-metrics)))


(reg-sub
  ::jvm-memory
  (fn [query-v _]
    (subscribe [::raw-metrics]))
  (fn [raw-metrics query-v _]
    (metrics-utils/extract-memory-metrics raw-metrics)))


(reg-sub
  ::ring-request-rates
  (fn [query-v _]
    (subscribe [::raw-metrics]))
  (fn [raw-metrics query-v _]
    (metrics-utils/extract-ring-requests-rates raw-metrics)))


(reg-sub
  ::ring-response-rates
  (fn [query-v _]
    (subscribe [::raw-metrics]))
  (fn [raw-metrics query-v _]
    (metrics-utils/extract-ring-responses-rates raw-metrics)))
