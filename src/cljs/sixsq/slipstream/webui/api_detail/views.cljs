(ns sixsq.slipstream.webui.api-detail.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.api-detail.events :as api-detail-events]
    [sixsq.slipstream.webui.api-detail.subs :as api-detail-subs]
    [sixsq.slipstream.webui.api.subs :as api-subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.utils.resource-details :as details]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]))


(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::api-detail-subs/loading?])
        resource-id (subscribe [::api-detail-subs/resource-id])]
    (fn []
      [ui/MenuMenu {:position "right"}
       [uix/MenuItemWithIcon
        {:name      (@tr [:refresh])
         :icon-name "refresh"
         :loading?  @loading?
         :on-click  #(dispatch [::api-detail-events/get @resource-id])}]])))


(defn path->resource-id
  [path]
  (str/join "/" (rest path)))


(defn api-detail
  []
  (let [cep (subscribe [::api-subs/cloud-entry-point])
        path (subscribe [::main-subs/nav-path])
        loading? (subscribe [::api-detail-subs/loading?])
        cached-resource-id (subscribe [::api-detail-subs/resource-id])
        resource (subscribe [::api-detail-subs/resource])
        description (subscribe [::api-detail-subs/description])]
    (fn []
      (let [resource-id (path->resource-id @path)
            correct-resource? (= resource-id @cached-resource-id)]

        ;; forces a refresh when the correct resource isn't cached
        (when-not correct-resource?
          (dispatch [::api-detail-events/get (path->resource-id @path)]))

        ;; render the (possibly empty) detail
        [details/resource-detail
         [refresh-button]
         (when (and (not @loading?) correct-resource?) @resource)
         (:baseURI @cep)
         @description]))))
