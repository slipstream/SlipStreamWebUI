(ns sixsq.slipstream.webui.plot.plot
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.general :as general]
    [cljsjs.chartjs]))


;; setup global defaults
(set! (.-defaultColor (.-global (.-defaults js/Chart))) "rgba(30, 144, 255, 0.75)")
(set! (.-backgroundColor (.-rectangle (.-elements (.-global (.-defaults js/Chart))))) "rgba(30, 144, 255, 0.75)")
(set! (.-display (.-legend (.-global (.-defaults js/Chart)))) false)


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
                       ;; FIXME: Must mutate the data directly via the chart instance.
                       #_(some-> @chartjs-instance .update)
                       (some-> @chartjs-instance .destroy)
                       (when-let [ctx (.getElementById js/document plot-id)]
                         (let [instance (js/Chart. ctx (clj->js plot-data))]
                           (reset! chartjs-instance instance)))
                       [:div {:class "chartjs-container"
                              :style {:position "relative", :width "100%", :height "100%"}}
                        [:canvas {:id plot-id}]])
       :component-will-unmount
                     #(some-> @chartjs-instance .destroy)})))


(defn chartjs-plot
  [plot-data]
  [chartjs-plot-inner plot-data])
