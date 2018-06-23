(ns sixsq.slipstream.webui.usage.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.usage.spec :as usage-spec]))

(reg-sub
  ::loading?
  ::usage-spec/loading?)

(reg-sub
  ::filter-visible?
  ::usage-spec/filter-visible?)

(reg-sub
  ::results
  ::usage-spec/results)

(reg-sub
  ::loading-connectors-list?
  ::usage-spec/loading-connectors-list?)

(reg-sub
  ::connectors-list
  ::usage-spec/connectors-list)

(reg-sub
  ::loading-users-list?
  ::usage-spec/loading-connectors-list?)

(reg-sub
  ::users-list
  ::usage-spec/users-list)

(reg-sub
  ::selected-user
  ::usage-spec/selected-user)

(reg-sub
  ::date-range
  ::usage-spec/date-range)
