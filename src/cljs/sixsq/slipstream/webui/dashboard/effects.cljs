(ns sixsq.slipstream.webui.dashboard.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.utils.general :as general-utils]))

(reg-fx
  ::get-virtual-machines
  (fn [[client params]]
    (go
      (let [virtual-machines (<! (cimi/search client "virtualMachines" (general-utils/prepare-params params)))]
        (dispatch [:sixsq.slipstream.webui.dashboard.events/set-virtual-machines virtual-machines])))))
