(ns sixsq.slipstream.webui.nuvlabox.views
  (:require
    [cljs.pprint :refer [cl-format]]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.nuvlabox.events :as nuvlabox-events]
    [sixsq.slipstream.webui.nuvlabox.subs :as nuvlabox-subs]
    [sixsq.slipstream.webui.nuvlabox.utils :as u]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.plot.plot :as plot]))


(defn controls
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::nuvlabox-subs/loading?])]
    (fn []
      [ui/Menu
       [ui/MenuItem {:name     "refresh"
                     :on-click #(dispatch [::nuvlabox-events/fetch-state-info])}
        [ui/Icon {:name    "refresh"
                  :loading @loading?}]
        (@tr [:refresh])]])))


(defn controls-detail
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::nuvlabox-subs/detail-loading?])]
    (fn []
      [ui/Menu
       [ui/MenuItem {:name     "refresh"
                     :on-click #(dispatch [::nuvlabox-events/fetch-detail])}
        [ui/Icon {:name    "refresh"
                  :loading @loading?}]
        (@tr [:refresh])]])))


(defn id-as-link
  [id]
  (let [mac (-> id (str/split #"/") second)
        on-click #(dispatch [::history-events/navigate (str "nuvlabox/" mac)])]
    [:a {:style {:cursor "pointer"} :on-click on-click} id]))


(defn nuvlabox-summary-table
  [{:keys [nuvlaboxStates] :as nb-info}]
  (let [rows (doall
               (for [{:keys [id updated nextCheck]} nuvlaboxStates]
                 ^{:key id}
                 [:tr
                  [:td (id-as-link id)]
                  [:td updated]
                  [:td nextCheck]]))]
    [:table (vec (concat [:tbody] rows))]))


(defn state-summary
  []
  (let [state-info (subscribe [::nuvlabox-subs/state-info])]
    (fn []
      (let [{:keys [stale active]} @state-info
            stale-count (:count stale)
            active-count (:count active)]
        [ui/Container {:fluid true}
         [controls]
         [cc/collapsible-card
          (str "stale nuvlabox machines (" stale-count ")")
          #_[:pre (with-out-str (cljs.pprint/pprint stale))]
          [nuvlabox-summary-table stale]]
         [cc/collapsible-card
          (str "active nuvlabox machines (" active-count ")")
          #_[:pre (with-out-str (cljs.pprint/pprint active))]
          [nuvlabox-summary-table active]]]))))


(defn hw-id
  [{:keys [bus-id device-id] :as device}]
  (str bus-id "." device-id))


(defn device-row-header
  []
  [ui/TableHeader
   [ui/TableHeaderCell "busy"]
   [ui/TableHeaderCell "bus"]
   [ui/TableHeaderCell "device"]
   [ui/TableHeaderCell "vendor"]
   [ui/TableHeaderCell "product"]
   [ui/TableHeaderCell "description"]])


(defn device-row
  [{:keys [bus-id device-id vendor-id product-id busy description] :as device}]
  ^{:key (hw-id device)}
  [ui/TableRow
   [ui/TableCell (if busy "busy" "free")]
   [ui/TableCell bus-id]
   [ui/TableCell device-id]
   [ui/TableCell vendor-id]
   [ui/TableCell product-id]
   [ui/TableCell description]])


(def load-stats-vega-spec
  {:$schema     "https://vega.github.io/schema/vega-lite/v2.0.json"
   :description "machine load"
   :layer       [{:mark     :bar
                  :encoding {:x {:field :percentage
                                 :type  "quantitative"
                                 :axis  {:ticks false
                                         :title " "}
                                 :scale {:domain [0, 100]}}
                             :y {:field :label
                                 :type  "ordinal"
                                 :axis  {:ticks false
                                         :title " "}
                                 :sort  nil}}}
                 {:mark     {:type     :text
                             :align    :left
                             :baseline :middle
                             :dx       3}
                  :encoding {:text {:field :value
                                    :type  "ordinal"}
                             :x    {:field :percentage
                                    :type  "quantitative"}
                             :y    {:field :label
                                    :type  "ordinal"}}}]})


(defn load-plot
  [_ load]
  (let [load-stats (u/load-statistics load)]
    [plot/plot load-stats-vega-spec {:values load-stats} :style {:float :left}]))



(defn load
  [cpu ram disks]
  [cc/collapsible-card
   [:span [ui/Icon {:name "thermometer half"}] " load"]
   #_[:div (with-out-str (cljs.pprint/pprint cpu))]
   #_[:div (with-out-str (cljs.pprint/pprint ram))]
   #_[:div (with-out-str (cljs.pprint/pprint disks))]
   #_[:div (with-out-str (cljs.pprint/pprint (u/load-statistics {:cpu cpu :ram ram :disks disks})))]
   [load-plot {} {:cpu cpu :ram ram :disks disks}]])


(defn usb-devices
  [usb]
  [cc/collapsible-card
   [:span [ui/Icon {:name "usb"}] " usb devices"]
   [ui/Table
    [device-row-header]
    (vec (concat [ui/TableBody] (mapv device-row (sort-by hw-id usb))))]])


(defn detail-table
  []
  (let [detail (subscribe [::nuvlabox-subs/detail])]
    (fn []
      (let [{:keys [cpu ram disks usb]} @detail]
        [ui/Container {:fluid true}
         [load cpu ram disks]
         [usb-devices usb]]))))


(defn state-detail
  []
  (let [detail (subscribe [::nuvlabox-subs/detail])]
    (fn []
      [ui/Container {:fluid true}
       [controls-detail]
       [detail-table]
       #_[:pre (with-out-str (cljs.pprint/pprint @detail))]])))


(defn state-info
  []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (let [[_ mac] @path
            n (count @path)
            children (case n
                       1 [[state-summary]]
                       2 [[state-detail]]
                       [[state-summary]])]
        (dispatch [::nuvlabox-events/set-mac mac])
        (vec (concat [:div] children))))))


(defmethod panel/render :nuvlabox
  [path]
  [state-info])
