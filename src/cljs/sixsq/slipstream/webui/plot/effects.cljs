(ns sixsq.slipstream.webui.plot.effects
  (:require
    [cljsjs.vega-embed]
    [re-frame.core :refer [dispatch reg-fx]]
    [taoensso.timbre :as log]))


(reg-fx
  ::render-plot
  (fn [[plot-id plot-defn options]]
    (when-let [vega-lite-div (.getElementById js/document plot-id)]
      (js/vegaEmbed vega-lite-div (clj->js plot-defn) (clj->js options)))))
