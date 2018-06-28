(ns sixsq.slipstream.webui.application.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.cimi :as cimi]))


(reg-fx
  ::get-module
  (fn [[client path]]
    (go
      (let [path-filter (str "path='" path "'")
            children-filter (str "parentPath='" path "'")

            {:keys [type id] :as project-metadata} (if-not (str/blank? path)
                                                     (-> (<! (cimi/search client "modules" {:$filter path-filter}))
                                                         :modules
                                                         first)
                                                     {:type "PROJECT"})

            module (if (not= "PROJECT" type)
                     (<! (cimi/get client id))
                     project-metadata)

            children (when (= type "PROJECT")
                       (:modules (<! (cimi/search client "modules" {:$filter children-filter}))))

            module-data (assoc module :children children)]

        (dispatch [:sixsq.slipstream.webui.application.events/set-module path module-data])))))
