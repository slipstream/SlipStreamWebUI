(ns sixsq.slipstream.webui.nuvlabox-detail.views
  (:require
    [cljs.pprint :refer [cl-format]]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.nuvlabox-detail.events :as nuvlabox-events]
    [sixsq.slipstream.webui.nuvlabox-detail.subs :as nuvlabox-subs]
    [sixsq.slipstream.webui.nuvlabox.utils :as u]
    [sixsq.slipstream.webui.plot.plot :as plot]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.resource-details :as details]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn controls-detail
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::nuvlabox-subs/loading?])]
    (fn []
      [ui/Menu
       [ui/MenuItem {:name     "refresh"
                     :on-click #(dispatch [::nuvlabox-events/fetch-detail])}
        [ui/Icon {:name    "refresh"
                  :loading @loading?}]
        (@tr [:refresh])]])))


(defn hw-id
  [{:keys [bus-id device-id] :as device}]
  (str bus-id "." device-id))


(defn device-row-header
  []
  [ui/TableHeader
   [ui/TableRow
    [ui/TableHeaderCell "busy"]
    [ui/TableHeaderCell "bus"]
    [ui/TableHeaderCell "device"]
    [ui/TableHeaderCell "vendor"]
    [ui/TableHeaderCell "product"]
    [ui/TableHeaderCell "description"]]])


(defn device-row
  [{:keys [bus-id device-id vendor-id product-id busy description] :as device}]
  ^{:key (hw-id device)}
  [ui/TableRow
   [ui/TableCell {:collapsing true} (if busy "busy" "free")]
   [ui/TableCell {:collapsing true} bus-id]
   [ui/TableCell {:collapsing true} device-id]
   [ui/TableCell {:collapsing true} vendor-id]
   [ui/TableCell {:collapsing true} product-id]
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
  [cc/collapsible-segment
   [:span [ui/Icon {:name "thermometer half"}] " load"]
   [load-plot {} {:cpu cpu :ram ram :disks disks}]])


(defn usb-devices
  [usb]
  [cc/collapsible-segment
   [:span [ui/Icon {:name "usb"}] " usb devices"]
   [ui/Table
    [device-row-header]
    (vec (concat [ui/TableBody] (mapv device-row (sort-by hw-id usb))))]])


(defn heartbeat
  [updated next-check]
  (let [updated-moment (time/parse-iso8601 updated)
        next-check-moment (time/parse-iso8601 next-check)

        check-ok? (time/after-now? next-check)
        icon (if check-ok? "heartbeat" "warning sign")

        msg-last (str "Last heartbeat was " (time/ago updated-moment) " (" updated ").")
        msg-next (if check-ok?
                   (str "Next heartbeat is expected " (time/ago next-check-moment) " (" next-check ").")
                   (str "Next heartbeat was expected " (time/ago next-check-moment) " (" next-check ")."))]

    [cc/collapsible-segment
     [:span [ui/Icon {:name icon}] " heartbeat"]
     [:div
      [:div msg-last]
      [:div msg-next]]]))


(defn select-metadata
  [data]
  (let [metadata-keys #{:id :resourceURI :name :description :created :updated}]
    (select-keys data metadata-keys)))


(defn metadata-row
  [[k v]]
  ^{:key k}
  [ui/TableRow [ui/TableCell {:collapsing true} k] [ui/TableCell v]])


(defn nb-metadata
  []
  (let [record (subscribe [::nuvlabox-subs/record])]
    (fn []
      (let [{:keys [id name description acl] :as data} @record
            rows (->> data
                      select-metadata
                      (map metadata-row)
                      vec)]
        [cc/metadata {:title       (or name id)
                      :subtitle    (-> (or id "unknown/unknown")
                                       (str/split #"/")
                                       second)
                      :description description
                      :icon        "computer"
                      :acl         acl}
         rows]))))


(defn state-table
  []
  (let [detail (subscribe [::nuvlabox-subs/state])]
    (fn []
      (when @detail
        (let [{:keys [cpu ram disks usb updated nextCheck]} @detail]
          [ui/Container {:fluid true}
           [heartbeat updated nextCheck]
           [load cpu ram disks]
           [usb-devices usb]])))))


(defn record-info
  []
  (let [record (subscribe [::nuvlabox-subs/record])
        metadata-keys #{:id :resourceURI
                        :name :description
                        :created :updated
                        :acl :operations}]
    (fn []
      (when @record
        (let [data (into {} (remove (fn [[k v]] (metadata-keys k)) @record))]
          (first (details/format-resource-data data {})))))))


(defn nb-detail
  []
  (let [detail (subscribe [::nuvlabox-subs/state])
        record (subscribe [::nuvlabox-subs/record])]
    (fn []
      (when (or (nil? @detail) (nil? @record))
        (dispatch [::nuvlabox-events/fetch-detail]))
      [ui/Container {:fluid true}
       [controls-detail]
       [nb-metadata]
       [state-table]
       [record-info]])))
