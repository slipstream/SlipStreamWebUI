(ns sixsq.slipstream.webui.cimi-detail.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.cimi.subs :as cimi-subs]
    [sixsq.slipstream.webui.cimi-detail.events :as cimi-detail-events]
    [sixsq.slipstream.webui.cimi-detail.subs :as cimi-detail-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.resource-details :as details]
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
