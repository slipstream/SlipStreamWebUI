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

;; All animation turned off.  See comment below about plot updates.
(-> chartjs-global .-animation (set! false))


(defn chartjs-plot-inner
  [plot-data]
  (let [plot-id (str "chartjs-" (general/random-element-id))
        chartjs-instance (atom nil)
        destroy-chart (fn []
                        (some-> @chartjs-instance .destroy))
        new-chart (fn [plot-data]
                    (destroy-chart)
                    (reset! chartjs-instance
                            (some-> (.getElementById js/document plot-id)
                                    (js/Chart. (clj->js plot-data)))))]
    (reagent/create-class
      {:display-name "chartjs-plot"
       :component-did-mount
                     (fn [comp]
                       (new-chart plot-data))
       :reagent-render
                     (fn [plot-data]

                       ;; This is a brutal way of updating data.  It recreates the chart
                       ;; instance each time the data changes.  The plot animation is
                       ;; turned off globally to make the transitions between the plots
                       ;; less disturbing.
                       ;;
                       ;; The correct solution is to mutate the chart object data directly.
                       ;; However, all my attempts to do this from clojurescript have
                       ;; not worked.  The data is correctly updated, but the .update
                       ;; function doesn't actually update the plot visualization.
                       (new-chart plot-data)

                       [:div {:class "chartjs-container"
                              :style {:position "relative", :width "100%", :height "100%"}}
                        [:canvas {:id plot-id}]])
       :component-will-unmount
                     destroy-chart})))


(defn chartjs-plot
  [plot-data]
  [chartjs-plot-inner plot-data])
