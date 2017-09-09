(ns sixsq.slipstream.webui.panel.app.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.modules :as modules]
    [taoensso.timbre :as log]))

(def ^:const module-fields #{:shortName :description :category :creation :version :logoLink})

(reg-fx
  :fx.webui.app/search
  (fn [[client url]]
    (go
      (let [module (if (nil? url) {} (<! (modules/get client url)))
            module-meta (select-keys (first (vals module)) module-fields)
            module-kids (if (nil? url)
                          (<! (modules/get-children client url))
                          (->> module
                               vals
                               first
                               :children
                               :item
                               (map :name)))]
        (log/info (count module-kids) "returned for url" url)
        (dispatch [:set-modules-data (assoc module-meta :children module-kids)])))))
