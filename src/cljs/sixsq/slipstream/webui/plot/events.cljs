(ns sixsq.slipstream.webui.plot.events
  (:require
    [re-frame.core :refer [reg-event-fx]]
    [sixsq.slipstream.webui.plot.effects :as plot-fx]))


(reg-event-fx
  ::render-plot
  (fn [{:keys [db]} [_ plot-id plot-spec plot-data options]]
    (let [plot-defn (assoc plot-spec :data plot-data)]
      {:db                   db
       ::plot-fx/render-plot [plot-id plot-defn options]})))
