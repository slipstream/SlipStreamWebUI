(ns sixsq.slipstream.webui.metrics.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.metrics.subs :as metrics-subs]
    [sixsq.slipstream.webui.metrics.events :as metrics-events]

    [sixsq.slipstream.webui.utils.semantic-ui :as ui]

    [sixsq.slipstream.webui.plot.plot :as plot]))


(defn refresh-button
  []
  (let [loading? (subscribe [::metrics-subs/loading?])]
    (fn []
      [ui/Button
       {:circular true
        :primary  true
        :icon     "refresh"
        :loading  @loading?
        :on-click #(dispatch [::metrics-events/fetch-metrics])}])))


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
      [plot/plot responses-vega-spec {:values @rates}])))


(defn metrics-info
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container
     [ui/Header {:as "h1"} (@tr [:metrics])]
     [refresh-button]
     [thread-plot]
     [memory-plot]
     [ring-request-rates]
     [ring-response-rates]]))


(defmethod panel/render :metrics
  [path]
  [metrics-info])
