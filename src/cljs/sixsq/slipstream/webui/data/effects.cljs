(ns sixsq.slipstream.webui.data.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.data.utils :as utils]
    [taoensso.timbre :as log]))


(reg-fx
  ::fetch-data
  (fn [[client time-period-filter cloud-filter full-text-search data-queries callback]]
    (go
      (when client
        (doseq [{:keys [id query-data]} data-queries]
          (let [filter (utils/join-and time-period-filter cloud-filter full-text-search query-data)]
            (callback id (<! (cimi/search client
                                          "serviceOffers"
                                          {:$filter      filter
                                           :$last        0
                                           :$aggregation "count:id"})))))))))
