(ns sixsq.slipstream.webui.nuvlabox.subs
  (:require
    [re-frame.core :refer [dispatch reg-sub subscribe]]
    [sixsq.slipstream.webui.nuvlabox.spec :as nuvlabox-spec]))


(reg-sub
  ::loading?
  ::nuvlabox-spec/loading?)


(reg-sub
  ::state-info
  ::nuvlabox-spec/state-info)


;; copy from CIMI resource

(reg-sub
  ::query-params
  ::nuvlabox-spec/query-params)


(reg-sub
  ::aggregations
  ::nuvlabox-spec/aggregations)


(reg-sub
  ::collection
  ::nuvlabox-spec/collection)


(reg-sub
  ::collection-name
  ::nuvlabox-spec/collection-name)


(reg-sub
  ::selected-fields
  ::nuvlabox-spec/selected-fields)


(reg-sub
  ::available-fields
  ::nuvlabox-spec/available-fields)


(reg-sub
  ::cloud-entry-point
  ::nuvlabox-spec/cloud-entry-point)


(reg-sub
  ::show-add-modal?
  ::nuvlabox-spec/show-add-modal?)


(reg-sub
  ::collections-templates-cache
  ::nuvlabox-spec/collections-templates-cache)


(reg-sub
  ::collection-templates
  :<- [::collections-templates-cache]
  (fn [collections-templates-cache [_ template-href]]
    (when (contains? collections-templates-cache template-href)
      (let [templates-info (template-href collections-templates-cache)]
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
  ::nuvlabox-spec/loading?)


(reg-sub
  ::filter-visible?
  ::nuvlabox-spec/filter-visible?)


(reg-sub
  ::state-selector
  ::nuvlabox-spec/state-selector)


