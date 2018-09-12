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

    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [taoensso.timbre :as log]))


(defn controls
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::metrics-subs/loading?])
        loading-job-info? (subscribe [::metrics-subs/loading-job-info?])]
    (fn []
      [ui/Menu {:borderless true}
       [uix/MenuItemWithIcon
        {:name      (@tr [:refresh])
         :icon-name "refresh"
         :loading?  (or @loading? @loading-job-info?)
         :on-click  (fn []
                      (dispatch [::metrics-events/fetch-metrics])
                      (dispatch [::metrics-events/fetch-job-info]))}]])))


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
      [ui/Segment style/basic
       [plot/plot job-stats-vega-spec {:values (:states @job-info)} :style {:float :left}]])))


(defn job-chartjs
  []
  (let [job-info (subscribe [::metrics-subs/job-info])]
    (let [sorted-data (->> @job-info
                           :states
                           (map (juxt :key :doc_count))
                           (sort-by first))
          chartjs-data {:type    "horizontalBar"
                        :data    {:labels   (mapv first sorted-data)
                                  :datasets [{:label           "job states"
                                              :data            (mapv second sorted-data)
                                              :backgroundColor ["rgba(255, 99, 132, 0.2)"
                                                                "rgba(54, 162, 235, 0.2)"
                                                                "rgba(255, 206, 86, 0.2)"
                                                                "rgba(75, 192, 192, 0.2)"
                                                                "rgba(153, 102, 255, 0.2)"]}]}
                        :options {:legend {:display false}
                                  :scales {:xAxes [{:type "linear"}]
                                           :yAxes [{:gridLines {:display false}}]}}}]
      (log/error (with-out-str (cljs.pprint/pprint @job-info)))
      [ui/Card
       [ui/CardContent
        [plot/chartjs-plot chartjs-data]]])))


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
      (let [{:keys [old stale blocked
                    states]} @job-info
            {:strs [QUEUED RUNNING SUCCESS FAILED]} (->> states
                                                         (map (juxt :key :doc_count))
                                                         (into {}))
            rate (success-rate SUCCESS FAILED)]
        [ui/Segment style/evenly
         [statistic "running" RUNNING]
         [statistic "queued" QUEUED]
         [statistic "success rate" rate]
         [statistic "old" old]
         [statistic "stale" stale]
         [statistic "blocked" blocked]]))))


(defn request-statistics
  []
  [cc/collapsible-segment
   "request statistics"
   [ring-request-rates]
   [ring-response-rates]])


(defn server-statistics
  []
  [cc/collapsible-segment
   "server statistics"
   [thread-plot]
   [memory-plot]])


(defn job-statistics
  []
  [cc/collapsible-segment
   "job statistics"
   [job-numbers]
   [job-plot]
   [job-chartjs]])


(defn example-plot
  []
  [cc/collapsible-segment
   "example-plot"
   [plot/chartjs-plot plot/example-plot-data]])


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
       [job-statistics]
       [example-plot]])))


(defmethod panel/render :metrics
  [path]
  [metrics-info])
