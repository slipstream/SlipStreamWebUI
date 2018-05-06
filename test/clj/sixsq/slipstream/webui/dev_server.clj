(ns sixsq.slipstream.webui.dev_server
  (:require
    [compojure.core :refer [GET defroutes]]
    [compojure.route :as route]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.util.response :as response]))


(defroutes routes
           (route/resources "/" {:root "public"})
           (GET "/" [] (response/content-type
                         (response/resource-response "webui.html" {:root "public"})
                         "text/html"))
           (route/not-found (response/content-type
                              (response/resource-response "webui.html" {:root "public"})
                              "text/html")))


(def http-handler
  (wrap-defaults routes site-defaults))
