(ns sixsq.slipstream.webui.cimi.subs
  (:require
    [re-frame.core :refer [dispatch reg-sub]]
    [sixsq.slipstream.webui.cimi.events :as cimi-events]
    [sixsq.slipstream.webui.cimi.spec :as cimi-spec]
    [taoensso.timbre :as log]))


(reg-sub
  ::query-params
  ::cimi-spec/query-params)


(reg-sub
  ::aggregations
  ::cimi-spec/aggregations)


(reg-sub
  ::collection
  ::cimi-spec/collection)


(reg-sub
  ::collection-name
  ::cimi-spec/collection-name)


(reg-sub
  ::selected-fields
  ::cimi-spec/selected-fields)


(reg-sub
  ::available-fields
  ::cimi-spec/available-fields)


(reg-sub
  ::cloud-entry-point
  ::cimi-spec/cloud-entry-point)


(reg-sub
  ::show-add-modal?
  ::cimi-spec/show-add-modal?)


(reg-sub
  ::collections-templates-cache
  ::cimi-spec/collections-templates-cache)


(reg-sub
  ::collection-templates
  :<- [::collections-templates-cache]
  (fn [collections-templates-cache [_ template-href]]
    ;(log/error "subscription" template-href)
    (if-let [templates-info (template-href collections-templates-cache)]
      (do
        ;(log/error "::collection-templates " templates-info)
        templates-info
          )
      (dispatch [::cimi-events/get-templates template-href]))))


(reg-sub
  ::loading?
  ::cimi-spec/loading?)
