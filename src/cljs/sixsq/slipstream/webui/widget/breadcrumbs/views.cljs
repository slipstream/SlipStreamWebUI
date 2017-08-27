(ns sixsq.slipstream.webui.widget.breadcrumbs.views
  (:require
    [re-com.core :refer [h-box label hyperlink gap]]
    [sixsq.slipstream.webui.panel.app.effects]
    [sixsq.slipstream.webui.panel.app.events]
    [sixsq.slipstream.webui.panel.app.subs]

    [reagent.core :as reagent]
    [taoensso.timbre :as log]))

(def default-separator "\u2022")

(defn format-crumb-widget
  [model on-change index]
  (let [label (str (get @model index))
        trim-value (subvec @model 0 (inc index))]
    [hyperlink
     :label label
     :disabled? (= (count @model) (inc index))
     :class "webui-breadcrumb-element"
     :on-click (fn []
                 (log/debug "breadcrumb path changing to" trim-value)
                 (on-change trim-value))]))

(defn breadcrumbs-widget
  "Provides an h-box of breadcrumbs joined by the given separator. The
   separator use the default if not specified. The on-change function is
   required to propagate the changed breadcrumb path. The function will receive
   a single argument with the changed path as a vector."
  [& {:keys [model on-change separator]
      :or   {separator default-separator}
      :as   args}]
  (fn []
    (let [separator-label [label
                           :label separator
                           :class "webui-breadcrumbs-separator"]
          crumbs (map (partial format-crumb-widget model on-change) (range (count @model)))]
      (log/debug "rendering breadcrumbs:" @model)
      [h-box
       :class "webui-breadcrumbs"
       :gap "0.5ex"
       :children (interpose separator-label crumbs)])))
