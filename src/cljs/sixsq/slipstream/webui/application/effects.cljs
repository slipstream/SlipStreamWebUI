(ns sixsq.slipstream.webui.application.effects
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
        (dispatch [:sixsq.slipstream.webui.application.events/set-module module-id module-data])))))
