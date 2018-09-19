(ns sixsq.slipstream.webui.metrics.views
  (:require
    [cljs.pprint :refer [cl-format]]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.metrics.events :as metrics-events]
    [sixsq.slipstream.webui.metrics.subs :as metrics-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.plot.plot :as plot]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]))


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


(defn jvm-thread-chartjs
  []
  (let [jvm-threads (subscribe [::metrics-subs/jvm-threads])]
    (let [chartjs-data {:type    "horizontalBar"
                        :data    {:labels   (mapv :state @jvm-threads)
                                  :datasets [{:data (mapv :threads @jvm-threads)}]}
                        :options {:scales {:xAxes [{:type "linear"}]
                                           :yAxes [{:gridLines {:display false}}]}}}]

      [ui/Card
       [ui/CardContent
        [ui/CardHeader "JVM Thread State Counts"]
        [plot/chartjs-plot chartjs-data]]])))


(defn jvm-memory-chartjs
  []
  (let [jvm-memory (subscribe [::metrics-subs/jvm-memory])]
    (let [chartjs-data {:type    "horizontalBar"
                        :data    {:labels   (mapv :type @jvm-memory)
                                  :datasets [{:data (mapv #(/ % 1000000000.) (mapv :memory @jvm-memory))}]}
                        :options {:scales {:xAxes [{:type "linear"}]
                                           :yAxes [{:gridLines {:display false}}]}}}]

      [ui/Card
       [ui/CardContent
        [ui/CardHeader "JVM Memory (GB)"]
        [plot/chartjs-plot chartjs-data]]])))


(defn request-rates-chartjs
  []
  (let [rates (subscribe [::metrics-subs/ring-request-rates])]
    (let [chartjs-data {:type    "horizontalBar"
                        :data    {:labels   (mapv :requests @rates)
                                  :datasets [{:data (mapv :rate @rates)}]}
                        :options {:scales {:xAxes [{:type "linear"}]
                                           :yAxes [{:gridLines {:display false}}]}}}]

      [ui/Card
       [ui/CardContent
        [ui/CardHeader "Request Rates (request/s)"]
        [plot/chartjs-plot chartjs-data]]])))


(defn response-rates-chartjs
  []
  (let [rates (subscribe [::metrics-subs/ring-response-rates])]
    (let [chartjs-data {:type    "horizontalBar"
                        :data    {:labels   (mapv :responses @rates)
                                  :datasets [{:data (mapv :rate @rates)}]}
                        :options {:scales {:xAxes [{:type "linear"}]
                                           :yAxes [{:gridLines {:display false}}]}}}]

      [ui/Card
       [ui/CardContent
        [ui/CardHeader "Response Rates (response/s)"]
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


(defn job-chartjs
  []
  (let [job-info (subscribe [::metrics-subs/job-info])]
    (let [sorted-data (->> @job-info
                           :states
                           (map (juxt :key :doc_count))
                           (sort-by first))
          chartjs-data {:type    "horizontalBar"
                        :data    {:labels   (mapv first sorted-data)
                                  :datasets [{:data (mapv second sorted-data)}]}
                        :options {:scales {:xAxes [{:type "linear"}]
                                           :yAxes [{:gridLines {:display false}}]}}}]

      [ui/Card
       [ui/CardContent
        [ui/CardHeader "Job States"]
        [job-numbers]
        [plot/chartjs-plot chartjs-data]]])))


(defn metrics-info
  []
  (let [raw-metrics (subscribe [::metrics-subs/raw-metrics])
        job-info (subscribe [::metrics-subs/job-info])
        device (subscribe [::main-subs/device])]
    (fn []
      (when (nil? @raw-metrics)
        (dispatch [::metrics-events/fetch-metrics]))
      (when (nil? @job-info)
        (dispatch [::metrics-events/fetch-job-info]))
      [ui/Container {:fluid true}
       [controls]
       [ui/CardGroup {:doubling true, :items-per-row (if (= :wide-screen @device) 3 2)}
        [request-rates-chartjs]
        [response-rates-chartjs]
        [jvm-thread-chartjs]
        [jvm-memory-chartjs]
        [job-chartjs]]])))


(defmethod panel/render :metrics
  [path]
  [metrics-info])
