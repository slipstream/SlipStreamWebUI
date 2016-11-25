(ns sixsq.slipstream.webui
  (:require
   [reagent.core :as reagent :refer [atom]]
   [sixsq.slipstream.client.api.cimi :as cimi]
   [sixsq.slipstream.client.api.cimi.async :as cimi-async]))

(enable-console-print!)

(def ^cimi/cimi client (delay (cimi-async/instance)))

(defn some-component []
  [:div
   [:h3 "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red"]
    " text."]])

(defn calling-component []
  [:div "Parent component"
   [some-component]])

(defn init []
  (reagent/render-component [calling-component]
                            (.getElementById js/document "container")))
