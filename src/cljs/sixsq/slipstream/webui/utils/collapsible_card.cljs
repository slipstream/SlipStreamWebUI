(ns sixsq.slipstream.webui.utils.collapsible-card
  (:require
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn title-card
  [title & children]
  [ui/Card {:fluid true}
   [ui/CardContent {:extra true}
    [ui/CardHeader
     [:h1 title]]]
   (when children
     [ui/CardContent
      (vec (concat [ui/CardDescription] children))])])


(defn collapsible-card
  [title & children]
  (let [visible? (reagent/atom true)]
    (fn [title & children]
      [ui/Card {:fluid true}
       [ui/CardContent
        [ui/CardHeader
         title
         [ui/Button {:floated  :right
                     :icon     (if @visible? "chevron down" "chevron up")
                     :on-click #(reset! visible? (not @visible?))}]]
        (when @visible?
          (vec (concat [ui/CardDescription] children)))]])))


(defn collapsible-card-extra
  [title & children]
  (let [visible? (reagent/atom true)]
    (fn [title & children]
      [ui/Card {:fluid true}
       [ui/CardContent
        [ui/CardHeader
         title
         [ui/Button {:floated  :right
                     :icon     (if @visible? "chevron down" "chevron up")
                     :on-click #(reset! visible? (not @visible?))}]]
        (when @visible?
          (vec (concat [ui/CardDescription] (butlast children))))]
       [ui/CardContent
        [ui/CardDescription (last children)]]])))
