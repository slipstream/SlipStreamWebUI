(ns sixsq.slipstream.webui.utils.values
  "General functions for rendering values."
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.history.events :as history-events]))


(defn href?
  "Returns true if the map contains an href attribute."
  [value]
  (and (map? value) (:href value)))


(defn as-href
  "Renders a link to the CIMI detail page associated with the href. Ignores
   other values of the map (if any)."
  [{:keys [href]}]
  [:a {:on-click #(dispatch [::history-events/navigate (str "cimi/" href)])
       :style    {:cursor "pointer"}}
   (str href)])


(defn href-coll?
  [value]
  (and (coll? value)
       (every? href? value)))


(defn as-href-coll
  [value]
  (vec (concat [:span] (interpose " " (map as-href value)))))


(defn format-value
  [value]
  (cond
    (href-coll? value) (as-href-coll value)
    (href? value) (as-href value)
    :else (str value)))
