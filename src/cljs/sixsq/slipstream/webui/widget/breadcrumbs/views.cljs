(ns sixsq.slipstream.webui.widget.breadcrumbs.views
  (:require
    [re-com.core :refer [h-box label hyperlink]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.panel.app.effects]
    [sixsq.slipstream.webui.panel.app.events]
    [sixsq.slipstream.webui.panel.app.subs]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [reagent.core :as reagent]
    [taoensso.timbre :as log]))

(defn format-crumb [s index]
  [hyperlink
   :label (str s)
   :on-click #(dispatch [:trim-breadcrumbs index])])

(defn breadcrumbs []
  (let [tr (subscribe [:webui.i18n/tr])
        crumbs (subscribe [:modules-breadcrumbs])]
    (fn []
      (let [home (@tr [:home])]
        [h-box
         :class "webui-breadcrumbs"
         :gap "0.5ex"
         :children
         (doall (interpose [label :label ">"] (map format-crumb (cons home @crumbs) (range))))]))))

(defn format-crumb-widget
  [model on-change index]
  (let [label (str (get @model index))
        trim-value (subvec @model 0 (inc index))]
    [hyperlink
     :label label
     :disabled? (= (count @model) (inc index))
     :class "webui-breadcrumb-element"
     :on-click (fn []
                 (log/info "breadcrumb path changing to" trim-value)
                 (on-change trim-value))]))

(defn breadcrumbs-widget
  "Provides an h-box of breadcrumbs joined by the given separator. The
   separator will default to '>' if not specified. The on-change function is
   required to propagate the changed breadcrumb path. The function will receive
   a single argument with the changed path as a vector."
  [& {:keys [model on-change separator]
      :or   {separator ">"}
      :as   args}]
  (fn []
    (let [separator-label [label
                           :label separator
                           :class "webui-breadcrumbs-separator"]
          crumbs (map (partial format-crumb-widget model on-change) (range (count @model)))]
      (log/error "Rendering breadcrumbs:" @model)
      [h-box
       :class "webui-breadcrumbs"
       :gap "0.5ex"
       :children (interpose separator-label crumbs)])))
