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

    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


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
        loading? (subscribe [::nuvlabox-subs/detail-loading?])
        ]
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


(defn device-row
  [{:keys [bus-id device-id vendor-id product-id busy description] :as device}]
  ^{:key (str bus-id "." device-id)}
  [:tr
   [:td (str busy)]
   [:td bus-id]
   [:td device-id]
   [:td vendor-id]
   [:td product-id]
   [:td description]])


(defn detail-table
  []
  (let [detail (subscribe [::nuvlabox-subs/detail])]
    (fn []
      (let [{:keys [cpu ram disks usb]} @detail]
        [ui/Container {:fluid true}
         [:div (with-out-str (cljs.pprint/pprint cpu))]
         [:div (with-out-str (cljs.pprint/pprint ram))]
         [:div (with-out-str (cljs.pprint/pprint disks))]
         [:table (vec (concat [:tbody] (mapv device-row usb)))]]))))


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
