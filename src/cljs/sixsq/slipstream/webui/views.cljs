(ns sixsq.slipstream.webui.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text alert-box
                         button md-icon-button label modal-panel throbber
                         single-dropdown hyperlink hyperlink-href p
                         selection-list] :refer-macros [handler-fn]]
    [re-com.buttons :refer [button-args-desc]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [re-frame.loggers :refer [console]]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils :as utils]))

(defn message
  []
  (let [message (subscribe [:message])]
    (fn []
      (if @message
        [modal-panel
         :child @message
         :wrap-nicely? true
         :backdrop-on-click #(dispatch [:clear-message])]))))

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
        username (atom "")
        password (atom "")]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if-not logged-in?
          [h-box
           :children [[input-text
                       :model username
                       :placeholder "username"
                       :change-on-blur? true
                       :on-change #(reset! username %)]
                      [input-text
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
   :children
   (doall (map (fn [{:keys [rel href]}] [hyperlink
                                         :label rel
                                         :on-click #(js/alert (str "Operation: " rel " on URL " href))]) ops))])

(declare format-value)

(defn format-list-entry
  [k v]
  (let [id (:id v)
        attrs (if id {:key id} {})]
    [:li attrs [:strong (or id k)] (format-value v)]))

(defn format-map-entry
  [[k v]]
  [:li [:strong k] " : " (if (= k :operations)
                           (format-operations v)
                           (format-value v))])

(defn as-map [m]
  [:ul (doall (map format-map-entry m))])

(defn as-vec [v]
  [:ul (doall (map format-list-entry (range) v))])

(defn format-value [v]
  (cond
    (map? v) (as-map v)
    (vector? v) (as-vec v)
    :else (str v)))

(defn search-results []
  (let [search-results (subscribe [:search-results])
        collection-name (subscribe [:search-collection-name])]
    (fn []
      (let [results @search-results]
        (if (instance? js/Error results)
          [h-box :children [[label :label (str results)]]]
          (let [entries (get results (keyword @collection-name) [])]
            [h-box
             :children [(format-value entries)]]))))))

(defn header-row [selected-fields]
  [h-box
   :class    "rc-div-table-header"
   :children (map (fn [s] [label :label s :width "250px"]) selected-fields)])

(defn data-row [selected-paths entry]
  [h-box
   :class    "rc-div-table-row"
   :children (->> selected-paths
                  (map (fn [path] (str (get-in entry path))))
                  (map (fn [s] [label :label s :width "250px"])))])

(defn data-table [selected-fields selected-paths entries]
  [v-box
   :gap "5px"
   :class    "rc-div-table"
   :children [[header-row selected-fields]
              (map (fn [entry] ^{:key (:id entry)} [data-row selected-paths entry]) entries)]])

(defn search-result-table []
  (let [search-results (subscribe [:search-results])
        collection-name (subscribe [:search-collection-name])
        selected-fields (subscribe [:search-selected-fields])]
    (fn []
      (let [results @search-results]
        (if (instance? js/Error results)
          [v-box :children [[label :label (str results)]]]
          (let [entries (get results (keyword @collection-name) [])
                selected-paths (map utils/id->path @selected-fields)]
            [data-table @selected-fields selected-paths entries]))))))

(defn search-header []
  (let [first-value (atom "1")
        last-value (atom "20")
        filter-value (atom "")]
    (fn []
      [h-box
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

(defn page-header []
  [h-box
   :children [[box
               :child [:img {:src    "assets/images/slipstream_logo.svg"
                             :height "30px"}]]
              [gap
               :size "1"]
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
                                         :on-change #(reset! selections %)]
                                        [h-box
                                         :gap "1"
                                         :children [[button
                                                     :label "update"
                                                     :on-click (fn []
                                                                 (reset! show? false)
                                                                 (dispatch [:set-selected-fields @selections]))]
                                                    [button
                                                     :label "cancel"
                                                     :on-click (fn []
                                                                 (reset! show? false))]]]]]])]])))

(defn control-bar []
  [h-box
   :children [[cloud-entry-point]
              [select-fields]
              [gap :size "1"]
              [search-header]]])

(defn results-bar []
  (let [search (subscribe [:search])]
    (fn []
      (let [{:keys [completed? results collection-name]} @search]
        (if (instance? js/Error results)
          [h-box
           :children [[label :label (str results)]]]
          [h-box
           :children [(if-not completed?
                        [throbber
                         :size :small])
                      (if results
                        (let [total (:count results)
                              n (count (get results (keyword collection-name) []))]
                          [label
                           :label (str n " / " total)]))
                      [gap :size "1"]
                      (if-let [ops (:operations results)]
                        (format-operations ops))]])))))

(defn page-footer []
  [h-box
   :children [[label
               :label "Copyright © 2016, SixSq Sàrl"]]])

(defn app []
  [v-box
   :gap "5px"
   :children [[message]
              [page-header]
              [control-bar]
              [results-bar]
              [search-result-table]
              #_[search-results]
              [page-footer]]])
