(ns sixsq.slipstream.ui.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(println "Hello, Lori")

(defn simple-example []
  [:div
   [:span "Hello World"]])

(defn ^:export run []
  (r/render [simple-example]
            (js/document.getElementById "app")))
