(ns sixsq.slipstream.scui.main.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text input-password alert-box
                         button row-button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p checkbox horizontal-pill-tabs
                         scroller selection-list title]]
    [re-com.buttons :refer [button-args-desc]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [re-frame.loggers :refer [console]]
    [clojure.string :as str]
    [sixsq.slipstream.scui.utils :as utils]
    [sixsq.slipstream.scui.apps.views :as apps-views]
    [sixsq.slipstream.scui.activity.views :as activity-views]
    [sixsq.slipstream.scui.offers.views :as offers-views]
    [sixsq.slipstream.scui.authn.views :as authn-views]))

(defn format-operations
  [ops]
  [h-box
   :gap "1ex"
   :children
   (doall (map (fn [{:keys [rel href]}] [hyperlink
                                         :label rel
                                         :on-click #(js/alert (str "Operation: " rel " on URL " href))]) ops))])

(declare format-data)

(defn format-list-entry
  [prefix k v]
  (let [id (:id v)]
    ^{:key (str prefix "-" k)}
    [:li [:strong (or id k)] (format-data prefix v)]))

(defn format-map-entry
  [prefix [k v]]
  ^{:key (str prefix "-" k)}
  [:li [:strong k] " : " (if (= k :operations)
                           (format-operations v)
                           (format-data prefix v))])

(defn as-map [prefix m]
  [:ul (doall (map (partial format-map-entry prefix) m))])

(defn as-vec [prefix v]
  [:ul (doall (map (partial format-list-entry prefix) (range) v))])

(defn format-data
  ([v]
   (format-data (str (random-uuid)) v))
  ([prefix v]
   (cond
     (map? v) (as-map prefix v)
     (vector? v) (as-vec prefix v)
     :else (str v))))

(defn branch? [v]
  (or (map? v) (vector? v)))

(defn create-label [k v]
  (if-not (branch? v)
    (str k " : " v)
    (str k)))

(declare tree)

(defn tree-node [indent prefix k v]
  (let [react-key (str prefix "-" k)
        indent-size (str (* 2 indent) "ex")
        tag (create-label k v)
        icon (if (branch? v)
               [md-icon-button
                :size :smaller
                :md-icon-name "zmdi-chevron-right"
                :disabled? false]
               [md-icon-button
                :size :smaller
                :md-icon-name "zmdi-stop"
                :disabled? true])]
    ^{:key react-key}
    [h-box
     :align :center
     :children [[gap :size indent-size]
                icon
                [label :label tag]]]))

(defn tree-reducer [indent prefix r k v]
  (let [parent (tree-node indent prefix k v)
        children (tree (inc indent) prefix v)]
    (concat r [parent] children)))

(defn tree [indent prefix data]
  (if (branch? data)
    (doall (reduce-kv (partial tree-reducer indent prefix) [] data))))

(defn tree-widget [indent prefix data]
  (if-let [tree-rows (tree indent prefix data)]
    [v-box
     :children tree-rows]))

(defn panel-controls []
  (let [model (subscribe [:panel])]
    (fn []
      [horizontal-pill-tabs
       :model model
       :tabs [{:id    :panel/apps
               :label "Apps"}
              {:id    :panel/offers
               :label "Offers"}
              {:id    :panel/dashboard
               :label "Dashboard"}]
       :on-change #(dispatch [:set-panel %])])))

(defn page-header []
  [h-box
   :justify :between
   :children [[panel-controls]
              [authn-views/authn-panel]]])

(defn page-footer []
  [h-box
   :justify :center
   :children [[label
               :label "Copyright © 2016-2017, SixSq Sàrl"]]])

(defn message-modal
  []
  (let [message (subscribe [:message])]
    (fn []
      (if @message
        [modal-panel
         :child @message
         :wrap-nicely? true
         :backdrop-on-click #(dispatch [:clear-message])]))))

(defn resource-modal
  []
  (let [resource-data (subscribe [:resource-data])]
    (fn []
      (if @resource-data
        [modal-panel
         :child [v-box
                 :gap "3px"
                 :children [[scroller
                             :scroll :auto
                             :width "500px"
                             :height "300px"
                             :child [v-box
                                     :children [[title
                                                 :label (:id @resource-data)
                                                 :level :level3
                                                 :underline? true]
                                                #_(format-data @resource-data)
                                                #_(tree (:id @resource-data) @resource-data)
                                                (tree-widget 0 (:id @resource-data) @resource-data)]]]
                            [button
                             :label "close"
                             :on-click #(dispatch [:clear-resource-data])]]]
         :backdrop-on-click #(dispatch [:clear-resource-data])]))))

(defn panels
  []
  (let [selected-panel (subscribe [:panel])]
    (fn []
      (case @selected-panel
        :panel/offers [offers-views/offers-panel]
        :panel/dashboard [activity-views/runs-panel]
        :panel/apps [apps-views/modules-panel]))))

(defn app []
  [v-box
   :gap "5px"
   :children [[message-modal]
              [resource-modal]
              [page-header]
              [panels]
              [page-footer]]])
