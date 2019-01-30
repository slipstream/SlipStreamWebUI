(ns sixsq.slipstream.webui.api.subs
  (:require
    [re-frame.core :refer [dispatch reg-sub]]
    [sixsq.slipstream.webui.api.events :as api-events]
    [sixsq.slipstream.webui.api.spec :as api-spec]))


(reg-sub
  ::query-params
  ::api-spec/query-params)


(reg-sub
  ::aggregations
  ::api-spec/aggregations)


(reg-sub
  ::collection
  ::api-spec/collection)


(reg-sub
  ::collection-name
  ::api-spec/collection-name)


(reg-sub
  ::selected-fields
  ::api-spec/selected-fields)


(reg-sub
  ::available-fields
  ::api-spec/available-fields)


(reg-sub
  ::cloud-entry-point
  ::api-spec/cloud-entry-point)


(reg-sub
  ::show-add-modal?
  ::api-spec/show-add-modal?)


(reg-sub
  ::collections-templates-cache
  ::api-spec/collections-templates-cache)


(reg-sub
  ::collection-templates
  :<- [::collections-templates-cache]
  (fn [collections-templates-cache [_ template-href]]
    (when (contains? collections-templates-cache template-href)
      (let [templates-info (template-href collections-templates-cache)]
        (when (neg-int? (:loaded templates-info))
          (dispatch [::api-events/get-templates (name template-href)]))
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
  ::api-spec/loading?)
