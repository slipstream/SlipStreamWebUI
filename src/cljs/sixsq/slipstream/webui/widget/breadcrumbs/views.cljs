(ns sixsq.slipstream.webui.widget.breadcrumbs.views
  (:require
    [re-com.core :refer [h-box v-box label hyperlink ]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.panel.app.effects]
    [sixsq.slipstream.webui.panel.app.events]
    [sixsq.slipstream.webui.panel.app.subs]

    [sixsq.slipstream.webui.widget.i18n.subs]))

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
