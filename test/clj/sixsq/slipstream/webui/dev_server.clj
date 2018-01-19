(ns sixsq.slipstream.webui.dev_server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]))

(defroutes routes
           (route/resources "/" {:root "public"})
           (GET "/" [] (-> (response/resource-response "webui.html" {:root "public"})
                           (response/content-type "text/html")))
           (route/not-found (-> (response/resource-response "webui.html" {:root "public"})
                                (response/content-type "text/html"))))

(def http-handler
  (-> routes
      (wrap-defaults site-defaults)))
