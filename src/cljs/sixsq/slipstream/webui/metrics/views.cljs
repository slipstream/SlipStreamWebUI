(ns sixsq.slipstream.webui.metrics.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.metrics.subs :as metrics-subs]
    [sixsq.slipstream.webui.metrics.events :as metrics-events]

    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]

    [sixsq.slipstream.webui.plot.plot :as plot]
    [reagent.core :as reagent]
    [taoensso.timbre :as log]))


(defn controls
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::metrics-subs/loading?])]
    (fn []
      [ui/Menu
       [ui/MenuItem {:name     "refresh"
                     :on-click #(dispatch [::metrics-events/fetch-metrics])}
        [ui/Icon {:name    "refresh"
                  :loading @loading?}]
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


(defn metrics-info
  []
  (let [raw-metrics (subscribe [::metrics-subs/raw-metrics])]
    (fn []
      (when (nil? @raw-metrics)
        (dispatch [::metrics-events/fetch-metrics]))
      [ui/Container {:fluid true}
       [controls]
       [request-statistics]
       [server-statistics]])))


(defmethod panel/render :metrics
  [path]
  [metrics-info])
