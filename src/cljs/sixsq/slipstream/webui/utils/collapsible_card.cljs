(ns sixsq.slipstream.webui.utils.collapsible-card
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.style :as style]
    [clojure.string :as str]))


(defn more-or-less
  [state-atom]
  (let [tr (subscribe [::i18n-subs/tr])
        more? state-atom]
    (fn [state-atom]
      (let [label (@tr (if @more? [:less] [:more]))
            icon-name (if @more? "caret down" "caret right")]
        [:a {:style    {:cursor "pointer"}
             :on-click #(reset! more? (not @more?))}
         [ui/Icon {:name icon-name}]
         label]))))


(defn metadata
  [{:keys [title subtitle description logo icon acl] :as module-meta} rows]
  (let [more? (reagent/atom false)]
    (fn [{:keys [title subtitle description logo icon acl] :as module-meta} rows]
      [ui/Card {:fluid true}
       [ui/CardContent
        (when logo
          [ui/Image {:floated "right", :size :tiny, :src (:href logo)}])
        [ui/CardHeader
         [ui/Icon {:name icon}]
         (cond-> title
                 (not (str/blank? subtitle)) (str " (" subtitle ")"))]
        (when description
          [ui/CardMeta
           [:p description]])
        [ui/CardDescription
         [more-or-less more?]
         (when (and @more? (seq rows))
           [ui/Table style/definition
            (vec (concat [ui/TableBody] rows))])]]])))



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
        [ui/Label {:as       :a
                   :corner   "right"
                   :size     "mini"
                   :icon     (if @visible? "chevron down" "chevron up")
                   :on-click #(reset! visible? (not @visible?))}]
        [ui/CardHeader title]
        (when @visible?
          (vec (concat [ui/CardDescription] children)))]])))


(defn collapsible-segment
  [title & children]
  (let [visible? (reagent/atom true)]
    (fn [title & children]
      [ui/Segment style/basic
       [ui/Menu {:attached "top", :borderless true, :class "webui-section-header"}
        [ui/MenuItem {:position "left"
                      :header   true}
         title]
        [ui/MenuItem {:position "right"
                      :on-click #(reset! visible? (not @visible?))}
         [ui/Icon {:name (if @visible? "chevron down" "chevron up")}]]]
       [ui/Transition {:visible       @visible?
                       :animation     "fade"
                       :duration      300
                       :unmountOnHide true}
        (vec (concat [ui/Segment {:attached true}]
                     children))]])))


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

