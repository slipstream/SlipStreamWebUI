(ns sixsq.slipstream.webui.dev_server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY defroutes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

;https://www.opensourcery.co.za/2016/05/27/smooth-client-side-routing-in-a-figwheel-only-project/
(defroutes routes
           (ANY "webui/*" _
                {:status 200
                 :headers {"Content-Type" "text/html; charset=utf-8"}
                 :body (io/input-stream (io/resource "public/index.html"))})
           (ANY "/webui/*" _
                {:status 200
                 :headers {"Content-Type" "text/html; charset=utf-8"}
                 :body (io/input-stream (io/resource "public/index.html"))}))

(def http-handler
  (-> routes
      (wrap-defaults site-defaults)))
