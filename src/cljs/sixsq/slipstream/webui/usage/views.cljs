(ns sixsq.slipstream.webui.usage.views
  (:require
    [cljs.pprint :as pprint]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.usage.events :as usage-events]
    [sixsq.slipstream.webui.usage.subs :as usage-subs]
    [sixsq.slipstream.webui.usage.utils :as u]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn date-range-menu-item
  [key [start end :as range]]
  (let [tr (subscribe [::i18n-subs/tr])
        date-range (subscribe [::usage-subs/date-range])]
    (fn [key [start end :as range]]
      (let [[date-after date-before] @date-range]
        ^{:key key}
        [ui/MenuItem
         {:name     (@tr [key])
          :active   (and (u/same-date? date-after start)
                         (u/same-date? date-before end))
          :on-click #(dispatch [::usage-events/set-date-range range])}]))))


(defn search-calendar []
  (let [tr (subscribe [::i18n-subs/tr])
        locale (subscribe [::i18n-subs/locale])
        date-range (subscribe [::usage-subs/date-range])]
    (fn []
      (let [[date-after date-before :as range] @date-range]
        [ui/FormGroup
         [ui/FormField
          [ui/Menu {:secondary true :vertical true :size "small"}
           [date-range-menu-item :today (u/date-range 0 0)]
           [date-range-menu-item :yesterday (u/date-range 1 1)]
           [date-range-menu-item :last-7-days (u/date-range 7 1)]
           [date-range-menu-item :last-30-days (u/date-range 30 1)]]]
         [ui/FormField
          [ui/DatePicker {:custom-input  (r/as-element [ui/Input {:label (@tr [:from])}])
                          :selected      date-after
                          :start-date    date-after
                          :end-date      date-before
                          :min-date      (time/days-before 90)
                          :max-date      (time/now)
                          :selects-start true
                          :locale        @locale
                          :fixed-height  true
                          :date-format   "ddd, D MMMM YYYY"
                          :on-change     #(dispatch [::usage-events/set-date-range [% date-before]])
                          }]]
         [ui/FormField
          [ui/DatePicker {:custom-input (r/as-element [ui/Input {:label (@tr [:to])}])
                          :selected     date-before
                          :start-date   date-after
                          :end-date     date-before
                          :locale       @locale
                          :fixed-height true
                          :date-format  "ddd, D MMMM YYYY"
                          :min-date     date-after
                          :max-date     (time/now)
                          :selects-end  true
                          :on-change    #(dispatch [::usage-events/set-date-range [date-after (.endOf % "day")]])
                          }]]]))))


(defn search-all-clouds-dropdown []
  (let [connectors-list (subscribe [::usage-subs/connectors-list])
        loading-connectors-list? (subscribe [::usage-subs/loading-connectors-list?])]
    (dispatch [::usage-events/get-connectors-list])
    (fn []
      [ui/FormField
       [ui/Dropdown
        {:fluid       true
         :icon        "cloud"
         :className   "icon"
         :labeled     true
         :button      true
         :placeholder "All clouds"
         :loading     @loading-connectors-list?
         :multiple    true
         :search      true
         :selection   true
         :onChange    #(dispatch [::usage-events/set-selected-connectors
                                  (-> (js->clj %2 :keywordize-keys true) :value)])
         :options     (map
                        #(let [connector-href %
                               connector-name (str/replace connector-href #"^connector/" "")]
                           {:key connector-href :value connector-href :text connector-name})
                        @connectors-list)}]])))


(defn search-users-dropdown []
  (let [selected-user (subscribe [::usage-subs/selected-user])
        users (subscribe [::usage-subs/users-list])
        loading? (subscribe [::usage-subs/loading-users-list?])]
    (fn []
      [ui/FormField
       [ui/ButtonGroup {:fluid true}
        [ui/Dropdown {:as          :div
                      :placeholder "Filter by user"
                      :search      true
                      :icon        "users"
                      :labeled     true
                      :button      true
                      :value       @selected-user
                      :className   "icon multiple"
                      :selection   true
                      :loading     @loading?
                      :onChange    #(dispatch [::usage-events/set-user (-> (js->clj %2 :keywordize-keys true) :value)])
                      :options     (map #(let [user-name (str/replace % #"^user/" "")]
                                           {:key user-name :value user-name :text user-name})
                                        @users)}]
        (when @selected-user
          [ui/Button {:compact true
                      :basic   true
                      :icon    "delete"
                      :floated "right"
                      :onClick #(dispatch [::usage-events/clear-user])}])]])))


(defn search-header []
  (let [is-admin? (subscribe [::authn-subs/is-admin?])
        loading? (subscribe [::usage-subs/loading-users-list?])]
    (fn []
      [ui/Form
       [ui/FormGroup {:widths "equal"}
        [search-calendar]
        [search-all-clouds-dropdown]
        (when @is-admin?
          (when @loading?
            (dispatch [::usage-events/get-users-list]))
          [search-users-dropdown])]])))


(defn filter-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        filter-visible? (subscribe [::usage-subs/filter-visible?])]
    (fn []
      [ui/MenuMenu {:position "right"}
       [ui/MenuItem {:name     "filter"
                     :on-click #(dispatch [::usage-events/toggle-filter])}
        [ui/IconGroup
         [ui/Icon {:name "filter"}]
         [ui/Icon {:name   (if @filter-visible? "chevron down" "chevron right")
                   :corner true}]]
        (str "\u00a0" (@tr [:filter]))]])))


(defn control-bar []
  (let [tr (subscribe [::i18n-subs/tr])
        filter-visible? (subscribe [::usage-subs/filter-visible?])]
    [:div
     [ui/Menu {:attached   "top"
               :borderless true}
      [ui/MenuItem {:name     "search"
                    :on-click #(dispatch [::usage-events/fetch-meterings])}
       [ui/Icon {:name "search"}]
       (@tr [:search])]
      [filter-button]]
     (when @filter-visible?
       [ui/Segment {:attached "bottom"}
        [search-header]])]))


(defn format [fmt-str & v]
  (apply pprint/cl-format nil fmt-str v))


(defn to-hour [v]
  (/ v 60))


(defn value-in-table [v]
  (let [v-hour (to-hour v)
        v-int-part (int v-hour)
        v-float-part (- v-hour v-int-part)]
    (format "~,,'',3:d~0,2f" v-int-part v-float-part)))


(defn value-in-statistic [v]
  (->> v to-hour Math/round (format "~,,'',3:d ")))


(defn to-GB-from-MB [v]
  (/ v 1024))


(defn statistics-all-cloud []
  (let [results (subscribe [::usage-subs/results])]
    (fn []
      (let [res-all-clouds (:all-clouds @results)]
        [ui/Segment {:padded false :basic true :textAlign "center" :style {:margin 0 :padding 0}}
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue "ALL"]
          [ui/StatisticLabel "CLOUDS"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (:vms res-all-clouds))
           [ui/Icon {:name "server"}]]
          [ui/StatisticLabel "VMs"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (:vcpu res-all-clouds))
           [ui/Icon {:size "small" :rotated "clockwise" :name "microchip"}]]
          [ui/StatisticLabel "CPU"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (to-GB-from-MB (:ram res-all-clouds)))
           [ui/Icon {:size "small" :name "grid layout"}]]
          [ui/StatisticLabel "RAM"]]
         [ui/Statistic {:size "tiny"}
          [ui/StatisticValue (value-in-statistic (to-GB-from-MB (:disk res-all-clouds)))
           [ui/Icon {:size "small" :name "database"}]]
          [ui/StatisticLabel {} "Disk"]]]))))


(defn results-table-row
  [[connector result]]
  ^{:key (name connector)}
  [ui/TableRow
   [ui/TableCell (str/replace (name connector) #"^connector/" "")]
   [ui/TableCell {:textAlign "right"} (value-in-table (:vms result))]
   [ui/TableCell {:textAlign "right"} (value-in-table (:vcpu result))]
   [ui/TableCell {:textAlign "right"} (value-in-table (to-GB-from-MB (:ram result)))]
   [ui/TableCell {:textAlign "right"} (value-in-table (to-GB-from-MB (:disk result)))]])


(defn table-results-clouds []
  (let [results (subscribe [::usage-subs/results])]
    (fn []
      [ui/Segment {:padded false :style {:margin 0 :padding 0} :basic true}
       [ui/Table {:striped true :fixed true}
        [ui/TableHeader
         [ui/TableRow
          [ui/TableHeaderCell "Cloud"]
          [ui/TableHeaderCell {:textAlign "right"} "VMs [h]"]
          [ui/TableHeaderCell {:textAlign "right"} "CPUs [h]"]
          [ui/TableHeaderCell {:textAlign "right"} "RAM [GB\u00b7h]"]
          [ui/TableHeaderCell {:textAlign "right"} "DISK [GB\u00b7h]"]]]
        [ui/TableBody
         (map results-table-row (sort-by first @results))]]])))


(defn search-result []
  (let [loading? (subscribe [::usage-subs/loading?])]
    [ui/Segment {:basic true, :loading @loading?}
     [statistics-all-cloud]
     [table-results-clouds]]))


(defn usage
  []
  [ui/Container {:fluid true}
   [control-bar]
   [search-result]])


(defmethod panel/render :usage
  [_]
  [usage])
