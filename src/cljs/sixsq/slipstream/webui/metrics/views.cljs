(ns sixsq.slipstream.webui.metrics.views
  (:require
    [cljs.pprint :refer [cl-format]]

    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]

    [sixsq.slipstream.webui.metrics.events :as metrics-events]
    [sixsq.slipstream.webui.metrics.subs :as metrics-subs]
    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.plot.plot :as plot]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]

    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn controls
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::metrics-subs/loading?])
        loading-job-info? (subscribe [::metrics-subs/loading-job-info?])]
    (fn []
      [ui/Menu
       [ui/MenuItem {:name     "refresh"
                     :on-click (fn []
                                 (dispatch [::metrics-events/fetch-metrics])
                                 (dispatch [::metrics-events/fetch-job-info]))}
        [ui/Icon {:name    "refresh"
                  :loading (or @loading? @loading-job-info?)}]
        (@tr [:refresh])]])))


(def thread-vega-spec
  {:$schema     "https://vega.github.io/schema/vega-lite/v2.0.json"
   :description "jvm threads"
   :mark        :bar
   :encoding    {:x {:field :threads
                     :type  "quantitative"}
                 :y {:field :state
                     :type  "ordinal"
                     :sort  {:field :threads
                             :op    "mean"}}}})

(defn thread-plot
  []
  (let [jvm-threads (subscribe [::metrics-subs/jvm-threads])]
    (fn []
      [plot/plot thread-vega-spec {:values @jvm-threads}])))


(def memory-vega-spec
  {:$schema     "https://vega.github.io/schema/vega-lite/v2.0.json"
   :description "jvm memory usage"
   :mark        :bar
   :encoding    {:x {:field :memory
                     :type  "quantitative"}
                 :y {:field :type
                     :type  "ordinal"
                     :sort  nil}}})

(defn memory-plot
  []
  (let [jvm-memory (subscribe [::metrics-subs/jvm-memory])]
    (fn []
      [plot/plot memory-vega-spec {:values @jvm-memory}])))


(def requests-vega-spec
  {:$schema     "https://vega.github.io/schema/vega-lite/v2.0.json"
   :description "ring request rates"
   :mark        :bar
   :encoding    {:x {:field :rate
                     :type  "quantitative"}
                 :y {:field :requests
                     :type  "ordinal"
                     :sort  nil}}})


(defn ring-request-rates
  []
  (let [rates (subscribe [::metrics-subs/ring-request-rates])]
    (fn []
      [plot/plot requests-vega-spec {:values @rates}])))


(def responses-vega-spec
  {:$schema     "https://vega.github.io/schema/vega-lite/v2.0.json"
   :description "ring response rates"
   :mark        :bar
   :encoding    {:x {:field :rate
                     :type  "quantitative"}
                 :y {:field :responses
                     :type  "ordinal"
                     :sort  nil}}})


(defn ring-response-rates
  []
  (let [rates (subscribe [::metrics-subs/ring-response-rates])]
    (fn []
      [plot/plot responses-vega-spec {:values @rates} :style {:float :right}])))


(def job-stats-vega-spec
  {:$schema     "https://vega.github.io/schema/vega-lite/v2.0.json"
   :description "job statistics"
   :layer       [{:mark     :bar
                  :encoding {:x {:field :doc_count
                                 :type  "quantitative"}
                             :y {:field :key
                                 :type  "ordinal"}}}
                 {:mark     {:type     :text
                             :align    :left
                             :baseline :middle
                             :dx       3}
                  :encoding {:text {:field :doc_count
                                    :type  "quantitative"}
                             :x    {:field :doc_count
                                    :type  "quantitative"}
                             :y    {:field :key
                                    :type  "ordinal"}}}]})


(defn job-plot
  []
  (let [job-info (subscribe [::metrics-subs/job-info])]
    (fn []
      [plot/plot job-stats-vega-spec {:values (:states @job-info)} :style {:float :left}])))


(defn success-rate
  [success failed]
  (let [success (or success 0)
        failed (or failed 0)
        total (+ success failed)]
    (if (zero? total)
      "0.0%"
      (str (cl-format nil "~,1F" (* 100. (/ success total))) "%"))))


(defn statistic
  [label value]
  ^{:key label}
  [ui/Statistic {:size "tiny"}
   [ui/StatisticValue value]
   [ui/StatisticLabel label]])


(defn job-numbers
  []
  (let [job-info (subscribe [::metrics-subs/job-info])]
    (fn []
      (let [{:keys [old stale blocked states]} @job-info
            {:strs [QUEUED RUNNING SUCCESS FAILED]} (->> states
                                                         (map (juxt :key :doc_count))
                                                         (into {}))
            rate (success-rate SUCCESS FAILED)]
        [:div
         [statistic "running" RUNNING]
         [statistic "queued" QUEUED]
         [statistic "success rate" rate]
         [statistic "old" old]
         [statistic "stale" stale]
         [statistic "blocked" blocked]]))))


(defn request-statistics
  []
  [cc/collapsible-card
   "request statistics"
   [ring-request-rates]
   [ring-response-rates]])


(defn server-statistics
  []
  [cc/collapsible-card
   "server statistics"
   [thread-plot]
   [memory-plot]])


(defn job-statistics
  []
  [cc/collapsible-card
   "job statistics"
   [job-numbers]
   [job-plot]])


(defn metrics-info
  []
  (let [raw-metrics (subscribe [::metrics-subs/raw-metrics])
        job-info (subscribe [::metrics-subs/job-info])]
    (fn []
      (when (nil? @raw-metrics)
        (dispatch [::metrics-events/fetch-metrics]))
      (when (nil? @job-info)
        (dispatch [::metrics-events/fetch-job-info]))
      [ui/Container {:fluid true}
       [controls]
       [request-statistics]
       [server-statistics]
       [job-statistics]])))


(defmethod panel/render :metrics
  [path]
  [metrics-info])
