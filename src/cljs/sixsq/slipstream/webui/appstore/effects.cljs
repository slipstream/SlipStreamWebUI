(ns sixsq.slipstream.webui.appstore.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [taoensso.timbre :as log]))


(reg-fx
  ::get-modules
  (fn [[client query-params callback]]
    (go
      (let [modules-data (<! (cimi/search client "modules" query-params))]
        (callback modules-data)))))

(reg-fx
  ::get-paths
  (fn [[client parent-path-search callback]]
    (go
      (let [projects (:modules (<! (cimi/search client "modules"
                                                {:$filter (str "type='PROJECT' and parentPath='" parent-path-search "'")
                                                 :$orderby "name"
                                                 :$select "id, name"})))]
        (callback projects)))))


(reg-fx
  ::get-deployment-templates
  (fn [[client module-id callback]]
    (when (and client module-id)
      (go
        (let [module-filter (str "module/href='" module-id "'")

              templates (-> (<! (cimi/search client "deploymentTemplates" {:$filter module-filter}))
                            :deploymentTemplates)]

          (callback (or templates [])))))))


(reg-fx
  ::deploy
  (fn [[client deployment-template-id callback]]
    (go
      (let [data {:deploymentTemplate {:href deployment-template-id}}
            {:keys [status resource-id] :as response} (<! (cimi/add client "deployments" data))]
        (when (and resource-id (= 201 status))
          (callback resource-id))))))


(reg-fx
  ::create-deployment-template
  (fn [[client module-id callback]]
    (go
      (let [data {:name (str "Deployment Template " module-id)
                  :description (str "A deployment template for the module " module-id)
                  :module {:href module-id}}
            {:keys [status resource-id] :as response} (<! (cimi/add client "deploymentTemplates" data))]
        (when (and resource-id (= 201 status))
          (callback resource-id))))))