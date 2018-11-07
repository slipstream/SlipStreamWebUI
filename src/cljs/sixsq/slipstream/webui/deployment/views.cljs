(ns sixsq.slipstream.webui.deployment.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-detail.views :as deployment-detail-views]
    [sixsq.slipstream.webui.deployment.events :as deployment-events]
    [sixsq.slipstream.webui.deployment.subs :as deployment-subs]
    [sixsq.slipstream.webui.history.views :as history]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.forms :as forms]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [sixsq.slipstream.webui.utils.general :as general-utils]
    [taoensso.timbre :as log]))


(defn control-bar []
  (let [tr (subscribe [::i18n-subs/tr])
        active-only? (subscribe [::deployment-subs/active-only?])]
    [ui/Form {:on-key-press (partial forms/on-return-key
                                     #(dispatch [::deployment-events/get-deployments]))}

     [ui/FormGroup
      [ui/FormField
       ^{:key (str "activeOnly:" @active-only?)}
       [ui/Checkbox {:defaultChecked @active-only?
                     :toggle         true
                     :fitted         true
                     :label          (@tr [:active?])
                     :on-change      (ui-callback/checked
                                       #(dispatch [::deployment-events/set-active-only? %]))}]]]]))


(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::deployment-subs/loading?])]
    [uix/MenuItemWithIcon
     {:name      (@tr [:refresh])
      :icon-name "refresh"
      :loading?  @loading?
      :on-click  #(dispatch [::deployment-events/get-deployments])}]))


(defn menu-bar
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn []
      [:div
       [ui/Menu {:attached "top", :borderless true}
        [refresh-button]
        [ui/MenuMenu {:position "right"}
         [ui/MenuItem
          [ui/Input {:placeholder (@tr [:search])
                     :icon        "search"
                     :on-change   (ui-callback/input-callback #(dispatch [::deployment-events/set-full-text-search %]))
                     }]]]]

       [ui/Segment {:attached "bottom"}
        [control-bar]]])))


(defn format-href
  [href]
  (let [tag (subs href 11 19)]
    [history/link (str href) tag]))


(defn row-fn [{:keys [id state module acl] :as deployment}]
  (let [deployments-creds-map (subscribe [::deployment-subs/deployments-creds-map])
        deployments-service-url-map (subscribe [::deployment-subs/deployments-service-url-map])
        service-url (get @deployments-service-url-map id)]
    ^{:key id}
    [ui/TableRow
     [ui/TableCell [format-href id]]
     [ui/TableCell state]
     [ui/TableCell "-"]                                     ;FIXME virtual machine mapping not available for cimi deployment
     [ui/TableCell (when service-url [:a {:href service-url :target "_blank", :rel "noreferrer"} service-url])]
     [ui/TableCell (:name module)]
     [ui/TableCell (:created deployment)]                   ;FIXME should be start time but not available in cimi deployment
     [ui/TableCell (str/join ", " (get @deployments-creds-map id ""))] ;FIXME
     [ui/TableCell (get-in acl [:owner :principal])]]))


(defn vertical-data-table
  [deployments-list]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [deployments-list]
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
         [ui/TableHeaderCell (@tr [:username])]]]
       (vec (concat [ui/TableBody]
                    (map row-fn deployments-list)))])))


(defn deployments-display
  [deployments]
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::deployment-subs/loading?])]
    (fn [deployments]
      (let [deployments-list (get deployments :deployments [])]
        [ui/Segment (merge style/basic
                           {:class-name "webui-x-autoscroll"
                            :loading    @loading?})

         [ui/MenuItem
          [ui/Statistic {:size "tiny"}
           [ui/StatisticValue (str (count deployments-list) "/" (get deployments :count "-"))]
           [ui/StatisticLabel (@tr [:results])]]]

         [vertical-data-table deployments-list]]))))


(defn deployments-main
  []
  (let [elements-per-page (subscribe [::deployment-subs/elements-per-page])
        page (subscribe [::deployment-subs/page])
        deployments (subscribe [::deployment-subs/deployments])]
    (fn []
      (let [total-pages (general-utils/total-pages (get @deployments :count 0) @elements-per-page)]
        [ui/Container {:fluid true}
         [menu-bar]
         [ui/Segment style/basic
          [deployments-display @deployments]]
         (when (> total-pages 1)
           [uix/Pagination
            {:totalPages   total-pages
             :activePage   @page
             :onPageChange (ui-callback/callback :activePage #(dispatch [::deployment-events/set-page %]))}])]))))


(defn deployment-resources
  []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (let [n (count @path)
            [collection-name resource-id] @path
            children (case n
                       1 [[deployments-main]]
                       2 [[deployment-detail-views/deployment-detail (str collection-name "/" resource-id)]]
                       [[deployments-main]])]
        (vec (concat [ui/Segment style/basic] children))))))


(defmethod panel/render :deployment
  [path]
  (dispatch [::deployment-events/get-deployments])
  [deployment-resources])
