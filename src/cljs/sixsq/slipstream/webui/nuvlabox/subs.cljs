(ns sixsq.slipstream.webui.nuvlabox.subs
  (:require
    [re-frame.core :refer [reg-sub subscribe]]
    [sixsq.slipstream.webui.nuvlabox.spec :as nuvlabox-spec]))


(reg-sub
  ::loading?
  ::nuvlabox-spec/loading?)


(reg-sub
  ::state-info
  ::nuvlabox-spec/state-info)


(reg-sub
  ::detail-loading?
  ::nuvlabox-spec/detail-loading?)


(reg-sub
  ::detail
  ::nuvlabox-spec/detail)
