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
  (fn [[client path callback]]
    (go
      (let [path (or path "")
            path-filter (str "path='" path "'")
            children-filter (str "parentPath='" path "'")

            {:keys [type id] :as project-metadata} (if-not (str/blank? path)
                                                     (-> (<! (cimi/search client "modules" {:$filter path-filter}))
                                                         :modules
                                                         first)
                                                     {:type        "PROJECT"
                                                      :name        "Applications"
                                                      :description "cloud applications at your service"})

            module (if (not= "PROJECT" type)
                     (<! (cimi/get client id))
                     project-metadata)

            children (when (= type "PROJECT")
                       (:modules (<! (cimi/search client "modules" {:$filter children-filter}))))

            module-data (assoc module :children children)]

        (callback module-data)))))


(reg-fx
  ::create-module
  (fn [[client path data callback]]
    (go
      (let [{:keys [status] :as response} (<! (cimi/add client "modules" data))]
        (when (= 201 status)
          (callback response))))))


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
