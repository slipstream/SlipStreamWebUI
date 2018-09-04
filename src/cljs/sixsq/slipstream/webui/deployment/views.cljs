(ns sixsq.slipstream.webui.deployment.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-detail.events :as deployment-detail-events]
    [sixsq.slipstream.webui.deployment-detail.views :as deployment-detail-views]
    [sixsq.slipstream.webui.deployment.events :as deployment-events]
    [sixsq.slipstream.webui.deployment.subs :as deployment-subs]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.history.views :as history]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.forms :as forms]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))


(defn bool->int [bool]
  (if bool 1 0))


(defn runs-control []
  (let [tr (subscribe [::i18n-subs/tr])
        query-params (subscribe [::deployment-subs/query-params])]
    (fn []
      (let [{:keys [offset limit cloud activeOnly]} @query-params]
        [ui/Form {:on-key-press (partial forms/on-return-key
                                         #(dispatch [::deployment-events/get-deployments]))}
         [ui/FormGroup
          [ui/FormField
           ^{:key (str "offset:" offset)}
           [ui/Input {:type         "number"
                      :min          0
                      :label        (@tr [:offset])
                      :defaultValue offset
                      :placeholder  "e.g. 0"
                      :on-blur      (ui-callback/input-callback
                                      #(dispatch [::deployment-events/set-query-params {:offset %}]))}]]

          [ui/FormField
           ^{:key (str "limit:" limit)}
           [ui/Input {:type         "number"
                      :min          0
                      :label        (@tr [:limit])
                      :defaultValue limit
                      :placeholder  "e.g. 20"
                      :on-blur      (ui-callback/input-callback
                                      #(dispatch [::deployment-events/set-query-params {:limit %}]))}]]]

         [ui/FormGroup
          [ui/FormField
           ^{:key (str "cloud:" cloud)}
           [ui/Input {:type         "text"
                      :label        (@tr [:cloud])
                      :defaultValue cloud
                      :placeholder  "e.g. exoscale-ch-dk"
                      :on-blur      (ui-callback/input-callback
                                      #(dispatch [::deployment-events/set-query-params {:cloud %}]))}]]
          [ui/FormField
           ^{:key (str "activeOnly:" activeOnly)}
           [ui/Checkbox {:defaultChecked (-> activeOnly js/parseInt zero? not)
                         :toggle         true
                         :fitted         true
                         :label          (@tr [:active?])
                         :on-change      (ui-callback/checked
                                           #(dispatch [::deployment-events/set-query-params
                                                       {:activeOnly (bool->int %)}]))}]]]]))))


(defn menu-bar
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::deployment-subs/loading?])]
    (fn []
      [:div
       [ui/Menu {:attached   "top"
                 :borderless true}
        [uix/MenuItemWithIcon
         {:name      (@tr [:refresh])
          :icon-name "refresh"
          :loading?  @loading?
          :on-click  #(dispatch [::deployment-events/get-deployments])}]]

       [ui/Segment {:attached "bottom"}
        [runs-control]]])))


(defn service-url
  [url status]
  [:span
   (if (and (= status "Ready") (not (str/blank? url)))
     [:a {:href url, :target "_blank", :rel "noreferrer"}
      [:i {:class (str "zmdi zmdi-hc-fw-rc zmdi-mail-reply")}]]
     "\u00a0")])


(defn format-module
  [module]
  (let [tag (second (reverse (str/split module #"/")))]
    (fn []
      [:span tag])))


(defn format-uuid
  [uuid]
  (let [tag (.substring uuid 0 8)]
    [history/link (str "deployment/" uuid) tag]))


(defn row-fn [entry]
  [ui/TableRow
   [ui/TableCell [format-uuid (:uuid entry)]]
   [ui/TableCell (:status entry)]
   [ui/TableCell (:activeVm entry)]
   [ui/TableCell [service-url (:serviceUrl entry) (:status entry)]]
   [ui/TableCell [format-module (:moduleResourceUri entry)]]
   [ui/TableCell (:startTime entry)]
   [ui/TableCell (:cloudServiceNames entry)]
   [ui/TableCell (:tags entry)]
   [ui/TableCell (:username entry)]])


(defn vertical-data-table
  [entries]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [entries]
      [ui/Table
       {:compact     "very"
        :single-line true
        :padded      false
        :unstackable true}
       [ui/TableHeader
        [ui/TableRow
         [ui/TableHeaderCell (@tr [:id])]
         [ui/TableHeaderCell (@tr [:status])]
         [ui/TableHeaderCell (@tr [:vms])]
         [ui/TableHeaderCell (@tr [:url])]
         [ui/TableHeaderCell (@tr [:module])]
         [ui/TableHeaderCell (@tr [:start])]
         [ui/TableHeaderCell (@tr [:cloud])]
         [ui/TableHeaderCell (@tr [:tags])]
         [ui/TableHeaderCell (@tr [:username])]]]
       (vec (concat [ui/TableBody]
                    (map row-fn entries)))])))


(defn runs-display
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::deployment-subs/loading?])
        deployments (subscribe [::deployment-subs/deployments])]
    [ui/Segment (merge style/basic
                       {:class-name "webui-x-autoscroll"
                        :loading    @loading?})

     (when-let [{:keys [runs]} @deployments]
       (let [{:keys [count totalCount]} runs]
         [ui/MenuItem
          [ui/Statistic {:size "tiny"}
           [ui/StatisticValue (str count "/" totalCount)]
           [ui/StatisticLabel (@tr [:results])]]]))

     (when-let [{:keys [runs]} @deployments]
       (let [{:keys [item]} runs]
         [vertical-data-table (if (map? item) [item] item)]))]))


(defn deployments
  []
  [ui/Container {:fluid true}
   [menu-bar]
   [ui/Segment style/basic
    [runs-display]]])


(defn deployment-resource
  []
  (let [path (subscribe [::main-subs/nav-path])
        query-params (subscribe [::main-subs/nav-query-params])]
    (fn []
      (let [[_ resource-id] @path]
        (dispatch [::deployment-detail-events/set-runUUID resource-id])
        (when @query-params
          (dispatch [::deployment-events/set-query-params @query-params])))
      (let [n (count @path)
            children (case n
                       1 [[deployments]]
                       2 [[deployment-detail-views/deployment-detail]]
                       [[deployments]])]
        (vec (concat [ui/Segment style/basic] children))))))


(defmethod panel/render :deployment
  [path]
  [deployment-resource])
