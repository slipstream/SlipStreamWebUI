(ns
  ^{:copyright "Copyright 2017, SixSq Sàrl"
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.application.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.modules :as modules]))


(def ^:const metadata-fields #{:shortName :description :category :creation :version :logoLink})


(reg-fx
  ::get-module
  (fn [[client module-id]]
    (go
      (let [module (if (nil? module-id) {} (<! (modules/get-module client module-id)))
            module-meta (-> module vals first (select-keys metadata-fields))
            children (if (nil? module-id)
                       (<! (modules/get-module-children client nil))
                       (->> module
                            vals
                            first
                            :children
                            :item
                            (map :name)))
            module-data (assoc module-meta :children children)]
        (dispatch [:cubic.application.events/set-module module-id module-data])))))
