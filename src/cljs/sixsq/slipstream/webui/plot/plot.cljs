(ns sixsq.slipstream.webui.plot.plot
  (:require
    [cljsjs.chartjs]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.general :as general]))


;; setup global defaults
(def dodger-blue "rgba(30, 144, 255, 0.5)")
(def dodger-blue-opaque "rgba(30, 144, 255, 1.00)")
(def chartjs-global (.-global (.-defaults js/Chart)))

(-> chartjs-global .-defaultColor (set! dodger-blue-opaque))
(-> chartjs-global .-elements .-rectangle .-backgroundColor (set! dodger-blue))
(-> chartjs-global .-elements .-rectangle .-borderColor (set! dodger-blue-opaque))
(-> chartjs-global .-elements .-rectangle .-borderWidth (set! 2))
(-> chartjs-global .-legend .-display (set! false))


(defn chartjs-plot-inner
  [plot-data]
  (let [plot-id (str "chartjs-" (general/random-element-id))
        chartjs-instance (atom nil)
        destroy-chart (fn []
                        (some-> @chartjs-instance .destroy))
        new-chart (fn [plot-data]
                    (destroy-chart)
                    (when-let [ctx (.getElementById js/document plot-id)]
                      (let [instance (js/Chart. ctx (clj->js plot-data))]
                        (reset! chartjs-instance instance))))]
    (reagent/create-class
      {:display-name "chartjs-plot"
       :component-did-mount
                     (fn [comp]
                       (new-chart plot-data))
       :reagent-render
                     (fn [plot-data]

                       ;; This is a brutal way of updating data.  It recreates the chart
                       ;; instance each time the data changes.  A better approach would
                       ;; be to update the data directly in the existing chart instance.
                       ;; Each value must be updated individually.  The rendering is then
                       ;; done with the (.update) function.
                       (new-chart plot-data)

                       [:div {:class "chartjs-container"
                              :style {:position "relative", :width "100%", :height "100%"}}
                        [:canvas {:id plot-id}]])
       :component-will-unmount
                     destroy-chart})))


(defn chartjs-plot
  [plot-data]
  [chartjs-plot-inner plot-data])
