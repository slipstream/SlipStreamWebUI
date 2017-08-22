(ns sixsq.slipstream.webui.widget.operations.views
  (:require
    [re-com.core :refer [h-box v-box box label title button modal-panel p scroller gap]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [cljsjs.codemirror.mode.javascript]
    [sixsq.slipstream.webui.widget.editor :as editor]

    [sixsq.slipstream.webui.widget.operations.events]
    [sixsq.slipstream.webui.widget.operations.effects]))

(defn add-button
  "Creates an add button that will bring up a dialog to create a new resource."
  [data action-fn]
  (let [show? (reagent/atom false)]
    (fn []
      [v-box
       :children [[button
                   :label [:span [:i.zmdi.zmdi-hc-fw-rc.zmdi-plus-square] " add" ]
                   :class "btn-primary"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [v-box
                             :gap "2ex"
                             :size "auto"
                             :children [[scroller
                                         :min-width "80ex"
                                         :min-height "10em"
                                         :child [v-box
                                                 :gap "1ex"
                                                 :children [[editor/cm-outer {:data data}]
                                                            [gap :size "2ex"]
                                                            [box
                                                             :class "webui-block-button"
                                                             :size "auto"
                                                             :child [button
                                                                     :label "add"
                                                                     :class "btn btn-primary btn-block"
                                                                     :disabled? false
                                                                     :on-click (fn []
                                                                                 (action-fn {})
                                                                                 (reset! show? false))]]
                                                            [box
                                                             :class "webui-block-button"
                                                             :size "auto"
                                                             :child [button
                                                                     :label "cancel"
                                                                     :class "btn btn-default btn-block"
                                                                     :disabled? false
                                                                     :on-click (fn []
                                                                                 (reset! show? false))]]]]]]]])]])))

(defn edit-button
  "Creates an edit button that will bring up an edit dialog and will save the
   modified resource when saved."
  [data action-fn]
  (let [show? (reagent/atom false)]
    (fn []
      [v-box
       :children [[button
                   :label [:span [:i.zmdi.zmdi-hc-fw-rc.zmdi-edit] " edit" ]
                   :class "btn-primary"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [v-box
                             :gap "2ex"
                             :size "auto"
                             :children [[scroller
                                         :min-width "80ex"
                                         :min-height "10em"
                                         :child [v-box
                                                 :gap "1ex"
                                                 :children [[editor/cm-outer {:data data}]
                                                            [gap :size "2ex"]
                                                            [box
                                                             :class "webui-block-button"
                                                             :size "auto"
                                                             :child [button
                                                                     :label "save"
                                                                     :class "btn btn-primary btn-block"
                                                                     :disabled? false
                                                                     :on-click (fn []
                                                                                 (action-fn {})
                                                                                 (reset! show? false))]]
                                                            [box
                                                             :class "webui-block-button"
                                                             :size "auto"
                                                             :child [button
                                                                     :label "cancel"
                                                                     :class "btn btn-default btn-block"
                                                                     :disabled? false
                                                                     :on-click (fn []
                                                                                 (reset! show? false))]]]]]]]])]])))

(defn delete-button
  "Creates a button that will bring up a delete dialog and will execute the
   delete when confirmed."
  [data action-fn]
  (let [show? (reagent/atom false)]
    (fn []
      [v-box
       :children [[button
                   :label [:span [:i.zmdi.zmdi-hc-fw-rc.zmdi-delete] " delete" ]
                   :class "btn-primary"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [v-box
                             :gap "2ex"
                             :width "auto"
                             :children [[title :level :level2 :label "Delete resource?"]
                                        [p "The resource identifier is " [:strong (:id data)] ". "
                                         "Delete operations " [:strong "cannot"] " be undone."]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "confirm"
                                                 :class "btn btn-danger btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (action-fn)
                                                             (reset! show? false))]]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "cancel"
                                                 :class "btn btn-default btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (reset! show? false))]]]]])]])))

(defn other-button
  "Creates a button that will bring up a dialog to confirm the given action."
  [label data action-fn]
  (let [show? (reagent/atom false)]
    (fn []
      [v-box
       :children [[button
                   :label label
                   :class "btn-primary"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [v-box
                             :gap "2ex"
                             :width "auto"
                             :children [[title :level :level2 :label (str "Execute action " label "?")]
                                        [p "Confirm executing action " [:strong label] " on " [:strong (:id data)] "."]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "confirm"
                                                 :class "btn btn-danger btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (action-fn)
                                                             (reset! show? false))]]
                                        [box
                                         :class "webui-block-button"
                                         :size "auto"
                                         :child [button
                                                 :label "cancel"
                                                 :class "btn btn-default btn-block"
                                                 :disabled? false
                                                 :on-click (fn []
                                                             (reset! show? false))]]]]])]])))

(defn operation-name [op-uri]
  (second (re-matches #"^(?:.*/)?(.+)$" op-uri)))

(defn operation-button [data [label href]]
  (case label
    "add" [add-button data (fn [data] (dispatch [:evt.webui.op/add href data]))]
    "edit" [edit-button data (fn [data] (dispatch [:evt.webui.op/edit href data]))]
    "delete" [delete-button data (fn [] (dispatch [:evt.webui.op/delete href]))]
    [other-button label data #(js/alert (str "operation not implemented yet: (GET) " href))]))

;; FIXME: This should directly take the operations field.  Should handle nil as well.
(defn format-operations [{:keys [operations] :as data} baseURI]
  (let [ops (map (juxt #(operation-name (:rel %)) #(str baseURI (:href %))) operations)]
    [h-box
     :gap "1ex"
     :children (map (partial operation-button data) ops)]))

