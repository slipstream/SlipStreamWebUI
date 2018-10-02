(ns sixsq.slipstream.webui.plot.plot
  (:require
    [reagent.core :as reagent]
    [cljsjs.react-chartjs-2]))


;; setup global defaults
(def dodger-blue "rgba(30, 144, 255, 0.5)")
(def dodger-blue-opaque "rgba(30, 144, 255, 1.00)")
(def chartjs-global (.-global (.-defaults js/Chart)))

(-> chartjs-global .-defaultColor (set! dodger-blue-opaque))
(-> chartjs-global .-elements .-rectangle .-backgroundColor (set! dodger-blue))
(-> chartjs-global .-elements .-rectangle .-borderColor (set! dodger-blue-opaque))
(-> chartjs-global .-elements .-rectangle .-borderWidth (set! 2))
(-> chartjs-global .-legend .-display (set! false))


(defn array-get [tag]
  (aget js/ReactChartjs2 tag))

(defn adapt-component [tag]
  (reagent/adapt-react-class (array-get tag)))

(def HorizontalBar (adapt-component "HorizontalBar"))

;(def Bar (adapt-component "Bar"))
;
;(def Doughnut (adapt-component "Doughnut"))
;
;(def Pie (adapt-component "Pie"))
;
;(def Line (adapt-component "Line"))
;
;(def Radar (adapt-component "Radar"))
;
;(def Polar (adapt-component "Polar"))
;
;(def Bubble (adapt-component "Bubble"))
;
;(def Scatter (adapt-component "Scatter"))
