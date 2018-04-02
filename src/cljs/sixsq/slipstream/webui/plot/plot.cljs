(ns sixsq.slipstream.webui.plot.plot
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.plot.events :as plot-evts]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.general :as general]))


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
