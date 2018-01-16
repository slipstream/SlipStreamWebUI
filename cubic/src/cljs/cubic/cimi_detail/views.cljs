(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.cimi-detail.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [subscribe dispatch]]

    [cubic.cimi.subs :as cimi-subs]
    [cubic.cimi-detail.events :as cimi-detail-events]
    [cubic.cimi-detail.subs :as cimi-detail-subs]
    [cubic.main.subs :as main-subs]
    [cubic.utils.semantic-ui :as ui]
    [cubic.utils.resource-details :as details]
    [taoensso.timbre :as log]))


(defn refresh-button
  []
  (let [loading? (subscribe [::cimi-detail-subs/loading?])
        resource-id (subscribe [::cimi-detail-subs/resource-id])]
    (fn []
      [ui/Button
       {:circular true
        :primary  true
        :icon     "refresh"
        :loading  @loading?
        :on-click #(dispatch [::cimi-detail-events/get @resource-id])}])))


(defn path->resource-id
  [path]
  (str/join "/" (rest path)))


(defn cimi-detail
  []
  (let [cep (subscribe [::cimi-subs/cloud-entry-point])
        path (subscribe [::main-subs/nav-path])
        loading? (subscribe [::cimi-detail-subs/loading?])
        cached-resource-id (subscribe [::cimi-detail-subs/resource-id])
        resource (subscribe [::cimi-detail-subs/resource])]
    (fn []
      (let [resource-id (path->resource-id @path)
            correct-resource? (= resource-id @cached-resource-id)]
        (when (and (not @loading?) (not= resource-id @cached-resource-id))
          (dispatch [::cimi-detail-events/get resource-id]))
        [details/resource-detail [refresh-button] resource-id (when correct-resource? @resource) (:baseURI @cep)]))))
