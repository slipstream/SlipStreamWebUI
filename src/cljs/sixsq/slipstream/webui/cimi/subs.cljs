(ns sixsq.slipstream.webui.cimi.subs
  (:require
    [re-frame.core :refer [reg-sub dispatch]]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]
    [sixsq.slipstream.webui.cimi.utils :as cimi-utils]
    [sixsq.slipstream.webui.cimi.events :as cimi-events]
    [taoensso.timbre :as log]))


(reg-sub
  ::query-params
  (fn [db _]
    (::cimi-spec/query-params db)))


(reg-sub
  ::aggregations
  (fn [db _]
    (::cimi-spec/aggregations db)))


(reg-sub
  ::collection
  (fn [db _]
    (::cimi-spec/collection db)))


(reg-sub
  ::collection-name
  (fn [db _]
    (::cimi-spec/collection-name db)))


(reg-sub
  ::selected-fields
  (fn [db _]
    (::cimi-spec/selected-fields db)))


(reg-sub
  ::available-fields
  (fn [db _]
    (::cimi-spec/available-fields db)))


(reg-sub
  ::cloud-entry-point
  (fn [db _]
    (::cimi-spec/cloud-entry-point db)))


(reg-sub
  ::show-add-modal?
  (fn [db _]
    (::cimi-spec/show-add-modal? db)))


(reg-sub
  ::collections-templates-cache
  (fn [db]
    (::cimi-spec/collections-templates-cache db)))


(reg-sub
  ::collection-templates
  :<- [::collections-templates-cache]
  (fn [collections-templates-cache [_ template-href]]
    (when (contains? collections-templates-cache template-href)
      (let [templates-info (-> collections-templates-cache template-href)]
        (when (neg-int? (:loaded templates-info))
          (dispatch [::cimi-events/get-templates (name template-href)]))
        templates-info))))


(reg-sub
  ::collection-templates-loading?
  :<- [::collections-templates-cache]
  (fn [collections-templates-cache [_ template-href]]
    (when (contains? collections-templates-cache template-href)
      (let [templates-info (template-href collections-templates-cache)
            loaded (:loaded templates-info)
            total (:total templates-info)]
        (or (neg-int? loaded) (< loaded total))))))


(reg-sub
  ::loading?
  (fn [db]
    (::cimi-spec/loading? db)))


(reg-sub
  ::filter-visible?
  (fn [db]
    (::cimi-spec/filter-visible? db)))


