(ns sixsq.slipstream.webui.plot.plot
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.plot.events :as plot-evts]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [cljsjs.chartjs :as chartjs]))


(def default-vega-opts
  {:actions  false
   :renderer :svg})


(defn plot-inner
  "Function that acts as the reagent component for the Vega-Lite div. This
   should only be used via the public component function."
  [plot-defn data {:keys [options] :as args}]
  (let [plot-id (str "plot-" (general/random-element-id))
        options (merge default-vega-opts options)]
    (reagent/create-class
      {:display-name "vega-lite-plot"
       :component-did-mount
                     (fn [comp]
                       (dispatch [::plot-evts/render-plot plot-id plot-defn data options]))
       :reagent-render
                     (fn [plot-spec data {:keys [options] :as args}]
                       (let [options (merge default-vega-opts options)]
                         (dispatch [::plot-evts/render-plot plot-id plot-spec data options]))
                       [ui/Image {:as     :span
                                  :id     plot-id
                                  :inline true}])})))


(defn plot
  "Provides a plot based on Vega-Lite. You must supply the plot description
   and data."
  [plot-defn data & {:as args}]
  [plot-inner plot-defn data args])


(def example-plot-data
  {:type    "horizontalBar"
   :data    {:labels   ["Red", "Blue", "Yellow", "Green", "Purple", "Orange"],
             :datasets [{:label           "# of Votes"
                         :data            [12, 19, 3, 5, 2, 3]
                         :backgroundColor ["rgba(255, 99, 132, 0.2)"
                                           "rgba(54, 162, 235, 0.2)"
                                           "rgba(255, 206, 86, 0.2)"
                                           "rgba(75, 192, 192, 0.2)"
                                           "rgba(153, 102, 255, 0.2)"
                                           "rgba(255, 159, 64, 0.2)"]
                         :borderColor     ["rgba(255,99,132,1)"
                                           "rgba(54, 162, 235, 1)"
                                           "rgba(255, 206, 86, 1)"
                                           "rgba(75, 192, 192, 1)"
                                           "rgba(153, 102, 255, 1)"
                                           "rgba(255, 159, 64, 1)"]
                         :borderWidth     1}]}
   :options {:scales {:yAxes [{:ticks {:beginAtZero true}}]}}})


(defn chartjs-plot-inner
  [plot-data]
  (let [plot-id (str "chartjs-" (general/random-element-id))
        chartjs-instance (atom nil)]
    (reagent/create-class
      {:display-name "chartjs-plot"
       :component-did-mount
                     (fn [comp]
                       (let [ctx (.getElementById js/document plot-id)
                             instance (js/Chart. ctx (clj->js plot-data))]
                         (reset! chartjs-instance instance)))
       :reagent-render
                     (fn [plot-data]
                       (some-> @chartjs-instance (.update (clj->js plot-data)))
                       [:div {:class "chartjs-container"
                              :style {:position "relative", :width "100%", :height "100%"}}
                        [:canvas {:id plot-id}]])
       :component-will-unmount
                     #(some-> @chartjs-instance .destroy)})))


(defn chartjs-plot
  [plot-data]
  [chartjs-plot-inner plot-data])
