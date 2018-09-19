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
        chartjs-instance (atom nil)]
    (reagent/create-class
      {:display-name "chartjs-plot"
       :component-did-mount
                     (fn [comp]
                       (reset! chartjs-instance
                               (some-> (.getElementById js/document plot-id)
                                       (js/Chart. (clj->js plot-data)))))
       :reagent-render
                     (fn [plot-data]
                       [:div {:class "chartjs-container"
                              :style {:position "relative", :width "100%", :height "100%"}}
                        [:canvas {:id plot-id}]])
       :component-did-update
                     (fn [comp]
                       (let [plot-data (first (rest (reagent/argv comp)))]
                         (when-let [labels (clj->js (get-in plot-data [:data :labels]))]
                           (set! (.. @chartjs-instance -data -labels) labels))
                         (doseq [[i dataset] (map-indexed vector (get-in plot-data [:data :datasets]))]
                           (set!
                             (.-data (aget (.. @chartjs-instance -data -datasets) i))
                             (clj->js (:data dataset)))))
                       (.update @chartjs-instance))
       :component-will-unmount
                     (fn [comp]
                       (some-> @chartjs-instance .destroy))})))


(defn chartjs-plot
  [plot-data]
  [chartjs-plot-inner plot-data])
