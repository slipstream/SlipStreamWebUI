(ns sixsq.slipstream.webui.deployment-detail.gantt
  (:require
    [re-frame.core :refer [dispatch subscribe]]

    [sixsq.slipstream.webui.plot.plot :as plot]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn insert-end
  "Given the two events, it will copy the 'start' time of the later event as
   the 'end' time of the earlier event."
  [[{end :start :as later} earlier]]
  (assoc earlier :end end))


(def terminal-states #{"Done" "Aborted" "Cancelled"})


(defn add-now-event
  "Adds an event to the beginning of the sequence that will provide the end
   time for the youngest event. The end time will be the time plus 0.5 minute,
   if the state is terminal. It will be the current time, if the state is not
   terminal."
  [events]
  (let [{:keys [state :start]} (first events)
        {time-zero :timestamp} (last events)
        start (if (terminal-states state)
                (+ start 0.5)
                (time/delta-minutes time-zero))]
    (cons {:start start, :state "Now"} events)))


(defn events-to-gantt
  "Converts a sequence of events into a sequence of gantt chart entries. The
   events are expected to have the :delta-time, :timestamp, and
   :content->:state keys. The output will be a sequence of maps ordered from
   the earliest to latest, with each map containing the :start, :end, and
   :state keys. Note that if the last state isn't terminal, then the :end value
   will be the current time relative to the first event. The input events must
   be ordered from the youngest to oldest."
  [events]
  (->> events
       (filter #(= "state" (:type %)))
       (map (juxt :delta-time :timestamp #(-> % :content :state)))
       (map #(zipmap [:start :timestamp :state] %))
       (add-now-event)
       (map #(select-keys % #{:start :end :state}))
       (partition 2 1)
       (map insert-end)
       reverse))


(def gantt-vega-spec
  {:$schema     "https://vega.github.io/schema/vega-lite/v2.0.json"
   :description "event gantt chart"
   :mark        :bar
   :encoding    {:x  {:field :start
                      :type  "quantitative"}
                 :x2 {:field :end
                      :type  "quantitative"}
                 :y  {:field :state
                      :type  "ordinal"
                      :sort  nil}}})


(defn gantt-plot
  "Produces a gantt plot from the given data. The data must follow the format
   produced by the events-to-gantt function."
  [gantt-data]
  [plot/plot gantt-vega-spec {:values gantt-data}])


