(ns sixsq.slipstream.webui.usage.views
  (:require
    [cljs.pprint :as pprint]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.usage.events :as usage-events]
    [sixsq.slipstream.webui.usage.subs :as usage-subs]
    [sixsq.slipstream.webui.usage.utils :as u]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.time :as time]
    [sixsq.slipstream.webui.utils.values :as values]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]))


(defn to-hour [v]
  (/ v 60))


(defn to-GB-from-MB [v]
  (/ v 1024))


(defn format [fmt-str & v]
  (apply pprint/cl-format nil fmt-str v))


(defn value-in-table [v]
  (let [v-int-part (int v)
        v-float-part (- v v-int-part)]
    (format "~,,'',3:d~0,2f" v-int-part v-float-part)))


(defn value-in-statistic [v]
  (->> v Math/round (format "~,,'',3:d ")))


(defn results-table-row
  [[credential {:keys [vms cpus ram disk price] :as result}]]
  (let [credentials-map @(subscribe [::usage-subs/credentials-map])]
    ^{:key (name credential)}
    [ui/TableRow
     [ui/TableCell (values/as-href {:href credential})]
     [ui/TableCell (values/as-href (get-in credentials-map [credential :connector]))]
     [ui/TableCell {:textAlign "right"} (value-in-table (:value vms))]
     [ui/TableCell {:textAlign "right"} (value-in-table (:value cpus))]
     [ui/TableCell {:textAlign "right"} (value-in-table (:value ram))]
     [ui/TableCell {:textAlign "right"} (value-in-table (:value disk))]
     [ui/TableCell {:textAlign "right"} (value-in-table (:value price))]]))


(defn table-results-credentials []
  (let [results (subscribe [::usage-subs/results])
        tr (subscribe [::i18n-subs/tr])]
    (fn []
      [ui/Table {:selectable true, :compact "very", :unstackable true}
       [ui/TableHeader
        [ui/TableRow
         [ui/TableHeaderCell "credential"]
         [ui/TableHeaderCell "cloud"]
         [ui/TableHeaderCell {:textAlign "right"} u/vms-unit]
         [ui/TableHeaderCell {:textAlign "right"} u/cpus-unit]
         [ui/TableHeaderCell {:textAlign "right"} u/ram-unit]
         [ui/TableHeaderCell {:textAlign "right"} u/disk-unit]
         [ui/TableHeaderCell {:textAlign "right"} u/price-unit]]]
       [ui/TableBody
        (doall (map results-table-row (sort-by first @results)))]])))


(defn statistics-all-credentials []
  (let [results (subscribe [::usage-subs/results])
        credentials-map (subscribe [::usage-subs/credentials-map])
        selected-credentials (subscribe [::usage-subs/selected-credentials])]
    (fn []
      (let [{:keys [vms cpus ram disk price]} (get @results u/all-credentials)
            all-creds-count (count @credentials-map)
            real-count-selected-creds (count @selected-credentials)
            count_selected_creds (if (zero? real-count-selected-creds)
                                   all-creds-count
                                   real-count-selected-creds)]
        [ui/Segment style/evenly
         [ui/StatisticGroup {:size "tiny"}
          [ui/Statistic
           [ui/StatisticValue (str count_selected_creds "/" all-creds-count)]
           [ui/StatisticLabel "CREDENTIALS"]]
          [ui/Statistic
           [ui/StatisticValue (value-in-statistic (:value vms))
            [ui/Icon {:name "server"}]]
           [ui/StatisticLabel u/vms-unit]]
          [ui/Statistic
           [ui/StatisticValue (value-in-statistic (:value cpus))
            [ui/Icon {:size "small" :rotated "clockwise" :name "microchip"}]]
           [ui/StatisticLabel u/cpus-unit]]
          [ui/Statistic
           [ui/StatisticValue (value-in-statistic (:value ram))
            [ui/Icon {:size "small" :name "grid layout"}]]
           [ui/StatisticLabel u/ram-unit]]
          [ui/Statistic
           [ui/StatisticValue (value-in-statistic (:value disk))
            [ui/Icon {:size "small" :name "database"}]]
           [ui/StatisticLabel {} u/disk-unit]]
          [ui/Statistic
           [ui/StatisticValue (value-in-statistic (:value price))
            [ui/Icon {:size "small" :name "euro"}]]
           [ui/StatisticLabel {} u/price-unit]]]]))))


(defn search-credentials-dropdown []
  (let [credentials-map (subscribe [::usage-subs/credentials-map])
        loading-credentials-map? (subscribe [::usage-subs/loading-credentials-map?])]
    (fn []
      [ui/FormField
       [ui/Dropdown
        {:fluid       true
         :icon        "key"
         :className   "icon"
         :labeled     true
         :button      true
         :placeholder "All credentials"
         :loading     @loading-credentials-map?
         :multiple    true
         :search      true
         :selection   true
         :onChange    #(dispatch [::usage-events/set-selected-credentials
                                  (-> (js->clj %2 :keywordize-keys true) :value)])
         :options     (map
                        #(let [{:keys [id name description connector]} %]
                           {:key     id
                            :value   id
                            :text    id
                            :content (reagent/as-element [ui/Header {:as "h5"} id
                                                          [ui/HeaderSubheader (str "connector: " (:href connector))]
                                                          [ui/HeaderSubheader (str "name: " name)]
                                                          [ui/HeaderSubheader (str "description: " description)]])})
                        (vals @credentials-map))}]])))


(defn search-users-roles-dropdown []
  (let [selected-users-roles (subscribe [::usage-subs/selected-users-roles])
        users-roles-list (reagent/atom [])]
    (fn []
      [ui/FormField
       [ui/Dropdown {:fluid          true
                     :placeholder    "Filter by users or roles"
                     :search         true
                     :multiple       true
                     :icon           "users"
                     :labeled        true
                     :button         true
                     :value          @selected-users-roles
                     :className      "icon multiple"
                     :style          {:width nil}
                     :selection      true
                     :allowAdditions true
                     :onChange       #(dispatch [::usage-events/set-users-roles (-> (js->clj %2 :keywordize-keys true) :value)])
                     :onAddItem      (ui-callback/callback :value #(swap! users-roles-list conj {:text %, :value %}))
                     :options        @users-roles-list}]])))


(defn search-header []
  (let [is-admin? (subscribe [::authn-subs/is-admin?])
        tr (subscribe [::i18n-subs/tr])
        date-range (subscribe [::usage-subs/date-range])
        locale (subscribe [::i18n-subs/locale])
        range-initial-val u/default-date-range
        range-dropdown (reagent/atom range-initial-val)]
    (fn []
      (let [[date-after date-before :as range] @date-range
            disable-calendar (not= "custom" @range-dropdown)]
        [ui/Form
         [ui/FormGroup
          [ui/FormField
           [ui/Dropdown {:labeled      true
                         :button       true
                         :className    "icon"
                         :icon         "time"
                         :selection    true
                         :options      (map (fn [k] {:text  (@tr [(keyword k)])
                                                     :value k}) (keys u/date-range-entries))
                         :defaultValue range-initial-val
                         :onChange     #(do
                                          (reset! range-dropdown (-> %2
                                                                     (js->clj :keywordize-keys true)
                                                                     :value))
                                          (dispatch [::usage-events/set-date-range
                                                     (get u/date-range-entries @range-dropdown)]))}]]
          [ui/FormField {:disabled disable-calendar}
           [ui/DatePicker {:custom-input  (reagent/as-element [ui/Input {:label (@tr [:from])}])
                           :selected      date-after
                           :start-date    date-after
                           :end-date      date-before
                           :min-date      (time/days-before 90)
                           :max-date      (time/now)
                           :selects-start true
                           :locale        @locale
                           :fixed-height  true
                           :date-format   "ddd, D MMMM YYYY"
                           :on-change     #(dispatch [::usage-events/set-date-range [% date-before]])}]]
          [ui/FormField {:disabled disable-calendar}
           [ui/DatePicker {:custom-input (reagent/as-element [ui/Input {:label (@tr [:to])}])
                           :selected     date-before
                           :start-date   date-after
                           :end-date     date-before
                           :locale       @locale
                           :fixed-height true
                           :date-format  "ddd, D MMMM YYYY"
                           :min-date     date-after
                           :max-date     (time/now)
                           :selects-end  true
                           :on-change    #(dispatch [::usage-events/set-date-range [date-after (.endOf % "day")]])}]]]
         [ui/FormGroup {:widths "equal"}
          (when @is-admin?
            [search-users-roles-dropdown])
          [search-credentials-dropdown]]]))))


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
        filter-visible? (subscribe [::usage-subs/filter-visible?])
        results (subscribe [::usage-subs/results])]
    (dispatch [::usage-events/get-credentials-map])
    (fn []
      [:div
       [ui/Menu {:attached "top", :borderless true}
        [ui/MenuItem {:name     "refresh"
                      :on-click #(dispatch [::usage-events/fetch-meterings])}
         [ui/Icon {:name "refresh"}]
         (@tr [:refresh])]
        (when @results
          [ui/MenuItem {:as       :a
                        :download "data.json"
                        :href     (->> (general/edn->json @results)
                                       (.encodeURIComponent js/window)
                                       (str "data:text/plain;charset=utf-8,"))}
           [ui/Icon {:name "download"}]
           (@tr [:download])])
        [filter-button]]
       (when @filter-visible?
         [ui/Segment {:attached "bottom"}
          [search-header]])])))


(defn search-result []
  (let [loading? (subscribe [::usage-subs/loading?])]
    [ui/Segment (merge style/autoscroll-x {:loading @loading?})
     [statistics-all-credentials]
     [table-results-credentials]]))


(defn usage
  []
  [ui/Container {:fluid true}
   [control-bar]
   [search-result]])


(defmethod panel/render :usage
  [_]
  [usage])
