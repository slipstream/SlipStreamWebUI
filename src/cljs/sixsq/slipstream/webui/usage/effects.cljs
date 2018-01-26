(ns sixsq.slipstream.webui.usage.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.webui.usage.utils :as usage-utils]
    [taoensso.timbre :as log]))

(reg-fx
  ::fetch-meterings
  (fn [[client
        date-after
        date-before
        user
        connectors
        callback]]
    (usage-utils/fetch-meterings client
                                 date-after
                                 date-before
                                 user
                                 connectors
                                 callback)))
