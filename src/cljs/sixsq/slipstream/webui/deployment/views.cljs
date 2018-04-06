(ns sixsq.slipstream.webui.deployment.views
  (:require
    [clojure.string :as str]
    [cljs.pprint :refer [cl-format]]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as reagent]

    [sixsq.slipstream.webui.application.subs :as application-subs]
    [sixsq.slipstream.webui.deployment.events :as deployment-events]
    [sixsq.slipstream.webui.deployment.subs :as deployment-subs]
    [sixsq.slipstream.webui.deployment-detail.events :as deployment-detail-events]
    [sixsq.slipstream.webui.deployment-detail.views :as deployment-detail-views]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.component :as ui-utils]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.general :as utils]))


(defn bool->int [bool]
  (if bool 1 0))


(defn offer-option
  [{:keys [id name currency price connector]}]
  (let [formatted-price (if (neg? price)
                          "no price"
                          (str currency " " (cl-format nil "~,4F" price) "/h"))
        formatted-name (if-not (str/blank? name)
                         (str " \u2014 " name)
                         "")
        label (str connector " \u2014 " formatted-price " " formatted-name)]
    {:key   id
     :text  label
     :value id}))


(defn offer-options
  [{:keys [connectors]}]
  (mapv offer-option connectors))


(defn offer-selector
  []
  (let [tr (subscribe [::i18n-subs/tr])
        place-and-rank-loading? (subscribe [::deployment-subs/place-and-rank-loading?])
        place-and-rank (subscribe [::deployment-subs/place-and-rank])]
    (fn []
      (let [options (offer-options (first (filter #(nil? (:node %)) (:components @place-and-rank))))]
        [ui/FormSelect {:loading     @place-and-rank-loading?
                        :placeholder (@tr [:offer])
                        :options     options}]))))


(defn general-parameters
  []
  [ui/Segment
   [ui/Header "general"]
   [ui/FormField
    [ui/Checkbox {:label   "SSH access"
                  :checked true}]]
   [offer-selector]
   [ui/FormInput {:placeholder "tags"}]])



(defn input-parameter-field
  [[name description defaultValue]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} name]
   [ui/TableCell
    [ui/Input (cond-> {:fluid       true
                       :placeholder name
                       :transparent true}
                      defaultValue (assoc :defaultValue defaultValue))]]
   [ui/TableCell {:collapsing true}
    (when-not (str/blank? description)
      [ui/Popup {:content description
                 :trigger (reagent/as-element [ui/Icon {:name "help circle"}])}])]])


(defn input-parameter-field-number
  [[name description defaultValue]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} name]
   [ui/TableCell
    [ui/Input (cond-> {:type        "number"
                       :min         0
                       :fluid       true
                       :placeholder name
                       :transparent true}
                      defaultValue (assoc :defaultValue defaultValue))]]
   [ui/TableCell {:collapsing true}
    (when-not (str/blank? description)
      [ui/Popup {:content description
                 :trigger (reagent/as-element [ui/Icon {:name "help circle"}])}])]])


(defn input-parameters-form
  []
  (let [tr (subscribe [::i18n-subs/tr])
        module (subscribe [::application-subs/module])]
    (fn []
      (let [{:keys [parameters]} @module]
        (when parameters
          (let [children (->> parameters
                              (filter #(= "Input" (:category %)))
                              (map (juxt :name :description :defaultValue))
                              (sort-by first)
                              (map input-parameter-field))]
            (when (seq children)
              [ui/Segment
               [ui/Header (@tr [:parameters])]
               [ui/Table {:definition  true
                          :compact     true
                          :single-line true}
                (vec (concat [ui/TableBody] children))]])))))))


(defn cpu-ram-disk
  []
  [ui/Segment
   [ui/Header "resources"]
   [ui/Table {:definition  true
              :compact     true
              :single-line true}
    [ui/TableBody
     [input-parameter-field-number ["CPU" "override the required number of CPUs"]]
     [input-parameter-field-number ["RAM" "override the required amount of RAM"]]
     [input-parameter-field-number ["disk" "override the required amount of disk"]]]]])


(defn deployment-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        target-module (subscribe [::deployment-subs/deployment-target])
        user-connectors (subscribe [::deployment-subs/user-connectors])
        form-id (utils/random-element-id)]
    (fn []
      (when (and (boolean @target-module) (nil? @user-connectors))
        (dispatch [::deployment-events/get-user-connectors]))
      (when (and (boolean @target-module) (not (nil? @user-connectors)))
        (dispatch [::deployment-events/place-and-rank @target-module @user-connectors]))
      [ui/Modal {:close-icon true
                 :open       (boolean @target-module)
                 :on-close   (fn []
                               (dispatch [::deployment-events/clear-deployment-target])
                               (dispatch [::deployment-events/clear-user-connectors]))}
       [ui/Header {:icon    "cloud"
                   :content (@tr [:deploy])}]
       [ui/ModalContent {:scrolling true}
        [ui/Header @target-module]
        [ui/Form {:id       form-id
                  :onSubmit (fn [& args] (with-out-str (cljs.pprint/pprint args)))}
         [general-parameters]
         [input-parameters-form]
         [cpu-ram-disk]]]
       [ui/ModalActions
        [ui/Button
         {:on-click #(dispatch [::deployment-events/clear-deployment-target])}
         (@tr [:cancel])]
        [ui/Button
         {:primary  true
          :on-click #(->> form-id js/document.getElementById .submit)}
         (@tr [:deploy])]]])))


(defn runs-control []
  (let [tr (subscribe [::i18n-subs/tr])
        query-params (subscribe [::deployment-subs/query-params])]
    (fn []
      (let [{:keys [offset limit cloud activeOnly]} @query-params]
        [ui/Form
         [ui/FormGroup
          [ui/FormField
           ^{:key (str "offset:" offset)}
           [ui/Input {:type         "number"
                      :min          0
                      :label        (@tr [:offset])
                      :defaultValue offset
                      :on-blur      #(dispatch
                                       [::deployment-events/set-query-params {:offset (-> %1 .-target .-value)}])
                      }]]

          [ui/FormField
           ^{:key (str "limit:" limit)}
           [ui/Input {:type         "number"
                      :min          0
                      :label        (@tr [:limit])
                      :defaultValue limit
                      :on-blur      #(dispatch
                                       [::deployment-events/set-query-params {:limit (-> %1 .-target .-value)}])}]]]

         [ui/FormGroup
          [ui/FormField
           ^{:key (str "cloud:" cloud)}
           [ui/Input {:type         "text"
                      :label        (@tr [:cloud])
                      :defaultValue cloud
                      :on-blur      #(dispatch
                                       [::deployment-events/set-query-params {:cloud (-> %1 .-target .-value)}])}]]
          [ui/FormField
           ^{:key (str "activeOnly:" activeOnly)}
           [ui/Checkbox {:defaultChecked (-> activeOnly js/parseInt zero? not)
                         :slider         true
                         :fitted         true
                         :label          (@tr [:active?])
                         :on-change      (ui-utils/callback
                                           :checked #(dispatch [::deployment-events/set-query-params
                                                                {:activeOnly (bool->int %)}]))}]]]]))))


(defn menu-bar
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::deployment-subs/loading?])
        filter-visible? (subscribe [::deployment-subs/filter-visible?])]
    (fn []
      [:div
       [ui/Menu {:attached   (if @filter-visible? "top" false)
                 :borderless true}
        [ui/MenuItem {:name     "refresh"
                      :on-click #(dispatch [::deployment-events/get-deployments])}
         [ui/Icon {:name    "refresh"
                   :loading @loading?}]
         (@tr [:refresh])]
        [ui/MenuMenu {:position "right"}
         [ui/MenuItem {:name     "filter"
                       :on-click #(dispatch [::deployment-events/toggle-filter])}
          [ui/IconGroup
           [ui/Icon {:name "filter"}]
           [ui/Icon {:name   (if @filter-visible? "chevron down" "chevron right")
                     :corner true}]]
          (str "\u00a0" (@tr [:filter]))]]]

       (when @filter-visible?
         [ui/Segment {:attached "bottom"}
          [runs-control]])])))


(defn service-url
  [url status]
  [:span
   (if (and (= status "Ready") (not (str/blank? url)))
     [:a
      {:href   url
       :target "_blank"}
      [:i {:class (str "zmdi zmdi-hc-fw-rc zmdi-mail-reply")}]]
     "\u00a0")])


(defn format-module
  [module]
  (let [tag (second (reverse (str/split module #"/")))]
    (fn []
      [:span tag])))


(defn format-uuid
  [uuid]
  (let [tag (.substring uuid 0 8)
        on-click #(dispatch [::history-events/navigate (str "deployment/" uuid)])]
    [:a {:style {:cursor "pointer"} :on-click on-click} tag]))


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
       {:compact     true
        :single-line true
        :padded      false}
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
    (fn []
      [:div {:class-name "webui-x-autoscroll"}
       (when-not @loading?
         (when-let [{:keys [runs]} @deployments]
           (let [{:keys [count totalCount]} runs]
             [ui/MenuItem
              [ui/Statistic {:size :mini}
               [ui/StatisticValue (str count "/" totalCount)]
               [ui/StatisticLabel (@tr [:results])]]])))
       (when-not @loading?
         (when-let [{:keys [runs]} @deployments]
           (let [{:keys [item]} runs]
             [vertical-data-table item])))])))


(defn deployments
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container {:fluid true}
     [menu-bar]
     [cc/collapsible-card
      (@tr [:results])
      [runs-display]]]))


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
        (vec (concat [:div] children))))))


(defmethod panel/render :deployment
  [path]
  [deployment-resource])
