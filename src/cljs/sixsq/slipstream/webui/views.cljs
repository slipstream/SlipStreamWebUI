(ns sixsq.slipstream.webui.views
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
    [sixsq.slipstream.webui.utils :as utils]))

(defn logout-buttons
  [user-id]
  [[button
    :label user-id
    :on-click #(js/alert (str "profile for " user-id))]
   [button
    :label "logout"
    :on-click #(dispatch [:logout])]])

(defn logout
  []
  (let [authn (subscribe [:authn])]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if logged-in?
          [h-box
           :children (logout-buttons (or user-id "unknown"))])))))

(defn login
  []
  (let [authn (subscribe [:authn])
        username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if-not logged-in?
          [h-box
           :gap "3px"
           :children [[input-text
                       :model username
                       :placeholder "username"
                       :change-on-blur? true
                       :on-change #(reset! username %)]
                      [input-password
                       :model password
                       :placeholder "password"
                       :change-on-blur? true
                       :on-change #(reset! password %)]
                      [button
                       :label "login"
                       :on-click (fn []
                                   (dispatch [:login {:username @username :password @password}])
                                   (reset! username "")
                                   (reset! password ""))]]])))))

(defn authn-panel []
  [h-box
   :children [[login]
              [logout]]])

(def common-keys
  #{:id :created :updated :acl :baseURI :resourceURI})

(defn format-link [k]
  (let [n (name k)]
    {:id n :label n}))

(defn cep-opts [cep]
  (let [ks (sort (remove common-keys (keys cep)))]
    (map format-link ks)))

(defn cloud-entry-point
  []
  (let [cep (subscribe [:cloud-entry-point])
        selected-id (atom nil)]
    (fn []
      [h-box
       :children [[single-dropdown
                   :model selected-id
                   :placeholder "resource type"
                   :width "250px"
                   :choices (doall (cep-opts @cep))
                   :on-change (fn [id]
                                (reset! selected-id id)
                                (dispatch [:new-search id]))]]])))

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

(declare rows)

(defn indented-row [indent prefix k v]
  (let [react-key (str prefix "-" k)
        indent-size (str (* indent 2) "ex")]
    ^{:key react-key}
    [h-box
     ;;:style {:display "inherit"}
     :children [(if-not (zero? indent) [line :size "3px" :color "grey"])
                [gap :size indent-size]
                [md-icon-button
                 :md-icon-name "zmdi-chevron-right"
                 :disabled? true]
                [label :label (create-label k v)]]]))

(defn rows [indent prefix value]
  (cond
    (map? value) (doall (map (fn [[k v]] [indented-row indent prefix k v]) value))
    (vector? value) (doall (map (fn [k v] [indented-row indent prefix k v]) (range) value))
    :else nil))

(defn tree-old
  [prefix data]
  [v-box
   :gap "2px"
   :children (rows 0 prefix data)])

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

(defn data-field [selected-field entry]
  (fn []
    (let [v (or (get-in entry (utils/id->path selected-field)) "\u00a0")
          align (if (re-matches #"[0-9\.-]+" (str v)) :end :start)]
      (if (= "id" selected-field)
        [box :align align :child [hyperlink :label v :on-click #(dispatch [:set-resource-data entry])]]
        [box :align align :child [label :label v]]))))

(defn column-header-with-key [selected-field]
  ^{:key (str "column-header-" selected-field)}
  [h-box
   :justify :between
   :gap "1ex"
   :align :center
   :class "data-column-header"
   :children [[label
               :label selected-field]
              (if-not (= "id" selected-field)
                [row-button
                 :md-icon-name "zmdi zmdi-close"
                 :mouse-over-row? true
                 :tooltip "remove column"
                 :on-click #(dispatch [:remove-selected-field selected-field])])]])

(defn data-field-with-key [selected-field entry]
  (let [k (str "data-" selected-field "-" (:id entry))]
    ^{:key k} [data-field selected-field entry]))

(defn data-column-with-key [entries selected-field]
  ^{:key (str "column-" selected-field)}
  [v-box
   :padding "0 5px 0"
   :children [(column-header-with-key selected-field)
              (doall (map (partial data-field-with-key selected-field) entries))]])

(defn vertical-data-table [selected-fields entries]
  [h-box
   :gap "5px"
   :children [(doall (map (partial data-column-with-key entries) selected-fields))]])

(defn search-vertical-result-table []
  (let [search-results (subscribe [:search-results])
        collection-name (subscribe [:search-collection-name])
        selected-fields (subscribe [:search-selected-fields])]
    (fn []
      (let [results @search-results]
        [scroller
         :scroll :auto
         :child (if (instance? js/Error results)
                  [box :child (format-data (ex-data results))]
                  (let [entries (get results (keyword @collection-name) [])]
                    [vertical-data-table @selected-fields entries]))]))))

(defn search-header []
  (let [first-value (reagent/atom "1")
        last-value (reagent/atom "20")
        filter-value (reagent/atom "")]
    (fn []
      [h-box
       :gap "3px"
       :children [[input-text
                   :model first-value
                   :placeholder "first"
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! first-value v)
                                (dispatch [:set-search-first v]))]
                  [input-text
                   :model last-value
                   :placeholder "last"
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! last-value v)
                                (dispatch [:set-search-last v]))]
                  [input-text
                   :model filter-value
                   :placeholder "filter"
                   :width "300px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! filter-value v)
                                (dispatch [:set-search-filter v]))]
                  [button
                   :label "search"
                   :on-click #(dispatch [:search])]]])))

(defn panel-controls []
  (let [model (reagent/atom :panel/apps)]
    (fn []
      [horizontal-pill-tabs
       :model model
       :tabs [{:id    :panel/apps
               :label "Apps"}
              {:id    :panel/offers
               :label "Offers"}
              {:id    :panel/dashboard
               :label "Dashboard"}]
       :on-change (fn [selected-panel]
                    (reset! model selected-panel)
                    (dispatch [:set-panel selected-panel]))])))

(defn page-header []
  [h-box
   :justify :between
   :children [[box
               :child [:img {:src    "assets/images/slipstream_logo.svg"
                             :height "30px"}]]
              [panel-controls]
              [authn-panel]]])

(defn select-fields []
  (let [available-fields (subscribe [:search-available-fields])
        selected-fields (subscribe [:search-selected-fields])
        selections (reagent/atom #{})
        show? (reagent/atom false)]
    (fn []
      (reset! selections @selected-fields)
      [h-box
       :children [[button
                   :label "fields"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click (fn []
                                          (reset! show? false)
                                          (dispatch [:set-selected-fields @selections]))
                     :child [v-box
                             :width "350px"
                             :children [[selection-list
                                         :model selections
                                         :choices available-fields
                                         :multi-select? true
                                         :disabled? false
                                         :height "200px"
                                         :on-change #(reset! selections %)]
                                        [h-box
                                         :justify :between
                                         :children [[button
                                                     :label "update"
                                                     :on-click (fn []
                                                                 (reset! show? false)
                                                                 (dispatch [:set-selected-fields @selections]))]
                                                    [button
                                                     :label "cancel"
                                                     :on-click (fn []
                                                                 (reset! show? false))]]]]]])]])))

(defn select-controls []
  [h-box
   :gap "3px"
   :children [[cloud-entry-point]
              [select-fields]]])

(defn control-bar []
  [h-box
   :justify :between
   :children [[select-controls]
              [search-header]]])

(defn results-bar []
  (let [search (subscribe [:search])]
    (fn []
      (let [{:keys [completed? results collection-name]} @search]
        (if (instance? js/Error results)
          [h-box
           :children [[label :label "ERROR"]]]
          [h-box
           :children [[box
                       :justify :center
                       :align :center
                       :width "30px"
                       :height "30px"
                       :child (if completed? "" [throbber :size :small])]
                      (if results
                        (let [total (:count results)
                              n (count (get results (keyword collection-name) []))]
                          [label
                           :label (str "Results: " n " / " total)])
                        [label :label "Results: ?? / ??"])
                      [gap :size "1"]
                      (if-let [ops (:operations results)]
                        (format-operations ops))]])))))

(defn page-footer []
  [h-box
   :justify :center
   :children [[label
               :label "Copyright © 2016, SixSq Sàrl"]]])

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

(defn runs-control []
  (let [offset (reagent/atom "1")
        limit (reagent/atom "10")
        cloud (reagent/atom "")
        activeOnly (reagent/atom true)]
    (fn []
      [h-box
       :gap "3px"
       :children [[input-text
                   :model offset
                   :placeholder "offset"
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! offset v)
                                (dispatch [:set-runs-params {:offset v}]))]
                  [input-text
                   :model limit
                   :placeholder "limit"
                   :width "75px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! limit v)
                                (dispatch [:set-runs-params {:limit v}]))]
                  [input-text
                   :model cloud
                   :placeholder "cloud"
                   :width "300px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! cloud v)
                                (dispatch [:set-runs-params {:cloud v}]))]
                  [checkbox
                   :model activeOnly
                   :label "active only?"
                   :on-change (fn [v]
                                (reset! activeOnly v)
                                (dispatch [:set-runs-params {:activeOnly (if v 1 0)}]))]
                  [button
                   :label "show runs"
                   :on-click #(dispatch [:runs-search])]
                  ]])))

(defn runs-panel
  []
  (let [runs-data (subscribe [:runs-data])]
    (fn []
      [v-box
       :gap "3px"
       :children [[runs-control]
                  (if @runs-data
                    [scroller
                     :scroll :auto
                     :width "500px"
                     :height "300px"
                     :child [v-box
                             :children [[title
                                         :label "RUNS"
                                         :level :level3
                                         :underline? true]
                                        (format-data @runs-data)]]])
                  ]])))

(defn modules-control []
  (let [path (reagent/atom "")]
    (fn []
      [h-box
       :gap "3px"
       :children [[input-text
                   :model path
                   :placeholder "module"
                   :width "150px"
                   :change-on-blur? true
                   :on-change (fn [v]
                                (reset! path v)
                                (dispatch [:set-module-path (if (str/blank? v) nil v)]))]
                  [button
                   :label "show modules"
                   :on-click #(dispatch [:modules-search])]]])))

(defn modules-panel
  []
  (let [modules-data (subscribe [:modules-data])]
    (fn []
      [v-box
       :gap "3px"
       :children [[modules-control]
                  (if @modules-data
                    [scroller
                     :scroll :auto
                     :width "500px"
                     :height "300px"
                     :child [v-box
                             :children [[title
                                         :label "MODULES"
                                         :level :level3
                                         :underline? true]
                                        (format-data @modules-data)]]])
                  ]])))

(defn offers-panel
  []
  [v-box
   :children [[control-bar]
              [results-bar]
              [search-vertical-result-table]]])

(defn panels
  []
  (let [selected-panel (subscribe [:panel])]
    (fn []
      (case @selected-panel
        :panel/offers [offers-panel]
        :panel/dashboard [runs-panel]
        :panel/apps [modules-panel]))))

(defn app []
  [v-box
   :gap "5px"
   :children [[message-modal]
              [resource-modal]
              [page-header]
              [panels]
              [page-footer]]])
