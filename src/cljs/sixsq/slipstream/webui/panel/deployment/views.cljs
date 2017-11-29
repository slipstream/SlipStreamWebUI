(ns sixsq.slipstream.webui.panel.deployment.views
  (:require
    [re-com.core :refer [h-box v-box input-text button row-button label
                         hyperlink-href checkbox popover-tooltip scroller]
     :refer-macros [handler-fn]]
    [sixsq.slipstream.webui.components.core :refer [column]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]

    [clojure.string :as str]
    [sixsq.slipstream.webui.panel.deployment.effects]
    [sixsq.slipstream.webui.panel.deployment.events]
    [sixsq.slipstream.webui.panel.deployment.subs]
    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.resource :as resource]
    [taoensso.timbre :as log]))

(defn runs-control []
  (let [tr (subscribe [:webui.i18n/tr])
        params (subscribe [:webui.deployment/runs-params])
        offset (reagent/atom (:offset @params))
        limit (reagent/atom (:limit @params))
        cloud (reagent/atom (:cloud @params))
        activeOnly (reagent/atom (not (zero? (:activeOnly @params))))]
    (fn []
      [h-box
       :gap "1ex"
       :align :center
       :children [[input-text
                   :model offset
                   :placeholder (@tr [:offset])
                   :width "10ex"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! offset v)
                                (dispatch [:evt.webui.deployment/set-params {:offset v}]))]
                  [input-text
                   :model limit
                   :placeholder (@tr [:limit])
                   :width "10ex"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! limit v)
                                (dispatch [:evt.webui.deployment/set-params {:limit v}]))]
                  [input-text
                   :model cloud
                   :placeholder (@tr [:cloud])
                   :width "30ex"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! cloud v)
                                (dispatch [:evt.webui.deployment/set-params {:cloud v}]))]
                  [checkbox
                   :model activeOnly
                   :label (@tr [:active?])
                   :on-change (fn [v]
                                (reset! activeOnly v)
                                (dispatch [:evt.webui.deployment/set-params {:activeOnly (if v 1 0)}]))]
                  [button
                   :label (@tr [:show])
                   :on-click #(dispatch [:evt.webui.deployment/search])]]])))

(defn service-url
  [url status]
  [h-box
   :width "2ex"
   :justify :center
   :children [(if (and (= status "Ready") (not (str/blank? url)))
                [hyperlink-href
                 :label [:i {:class (str "zmdi zmdi-hc-fw-rc zmdi-mail-reply")}]
                 :href url
                 :target "_blank"]
                "\u00a0")]])

(defn format-module
  [module]
  (let [showing? (reagent/atom false)
        tag (second (reverse (str/split module #"/")))]
    (fn []
      (let [module-label [label
                          :width "15em"
                          :label tag
                          :attr {:on-mouse-over (handler-fn (reset! showing? true))
                                 :on-mouse-out  (handler-fn (reset! showing? false))}]]
        [popover-tooltip
         :label module
         :showing? showing?
         :anchor module-label]))))

(defn format-uuid
  [uuid]
  (let [showing? (reagent/atom false)
        tag (.substring uuid 0 8)]
    (fn []
      (let [uuid-label [label
                        :width "6em"
                        :label tag
                        :attr {:on-mouse-over (handler-fn (reset! showing? true))
                               :on-mouse-out  (handler-fn (reset! showing? false))}]]
        [popover-tooltip
         :label uuid
         :showing? showing?
         :anchor uuid-label]))))

(defn abort-popup [message]
  (fn []
    [h-box
     :width "2ex"
     :justify :center
     :children [(if-not (str/blank? message)
                  [row-button
                   :md-icon-name "zmdi zmdi-notifications-active"
                   :mouse-over-row? true
                   :tooltip message
                   :on-click #()]
                  [label :label "\u00a0"])]]))

(defn deployment-icon [type]
  (let [icon (case type
               "Orchestration" "zmdi-apps"
               "Run" "zmdi-widgets"
               "zmdi-minus")]
    [row-button
     :md-icon-name (str "zmdi " icon)
     :mouse-over-row? true
     :on-click #()]))

(def column-kws [:type :error :url :id :module :vms :status :username :start :cloud :tags])

(defn value-fn
  [column-kw entry]
  (case column-kw
    :type [label :label [deployment-icon (:type entry)]]
    :error [abort-popup (:abort entry)]
    :url [service-url (:serviceUrl entry) (:status entry)]
    :id [format-uuid (:uuid entry)]
    :module [format-module (:moduleResourceUri entry)]
    :vms (:activeVm entry)
    :start (:startTime entry)
    :cloud (:cloudServiceNames entry)
    (column-kw entry)))

(defn vertical-data-table
  [entries]
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn [entries]
      (let [entries (if (map? entries) [entries] entries)]  ;; wrap a single value in a list
        [scroller
         :scroll :auto
         :child [h-box
                 :class "webui-column-table"
                 :children [(doall
                              (for [column-kw column-kws]
                                ^{:key (name column-kw)} [column
                                                          :model entries
                                                          :key-fn :uuid
                                                          :value-fn (partial value-fn column-kw)
                                                          :header (@tr [column-kw])
                                                          :class "webui-column"
                                                          :header-class "webui-column-header"
                                                          :value-class "webui-column-value"]))]]]))))

(defn runs-display
  []
  (let [runs-data (subscribe [:webui.deployment/runs-data])]
    (fn []
      (if-let [{:keys [runs]} @runs-data]
        (let [{:keys [count totalCount item]} runs]
          [v-box
           :gap "0.25ex"
           :children [[label
                       :label (str count "/" totalCount)]
                      [vertical-data-table item]]])))))

(defn deployment-resource
  []
  (fn []
    [v-box
     :gap "0.25ex"
     :children [[runs-control]
                [runs-display]]]))

(defmethod resource/render "deployment"
  [path query-params]
  [deployment-resource])
