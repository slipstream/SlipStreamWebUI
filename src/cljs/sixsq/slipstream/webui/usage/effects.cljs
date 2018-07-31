(ns sixsq.slipstream.webui.usage.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.webui.usage.utils :as usage-utils]))


(reg-fx
  ::fetch-meterings
  (fn [[client
        date-after
        date-before
        user
        credentials
        callback]]
    (usage-utils/fetch-meterings client
                                 date-after
                                 date-before
                                 user
                                 credentials
                                 callback)))
