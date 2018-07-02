(ns sixsq.slipstream.webui.nuvlabox.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.webui.nuvlabox.utils :as u]))


(reg-fx
  ::fetch-state-info
  (fn [[client callback]]
    (go
      (let [stale (<! (u/nuvlabox-search client u/stale-nb-machines))
            active (<! (u/nuvlabox-search client u/active-nb-machines))]
        (callback {:stale stale, :active active})))))
