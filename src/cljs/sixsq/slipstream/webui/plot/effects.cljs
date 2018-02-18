(ns sixsq.slipstream.webui.plot.effects
  (:require
    [re-frame.core :refer [reg-fx dispatch]]
    [cljsjs.vega-embed]
    [taoensso.timbre :as log]))


(reg-fx
  ::render-plot
  (fn [[plot-id plot-defn options]]
    (when-let [vega-lite-div (.getElementById js/document plot-id)]
      (js/vegaEmbed vega-lite-div (clj->js plot-defn) (clj->js options)))))
