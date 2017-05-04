(ns sixsq.slipstream.webui.run
  (:require [ring.util.response :as r]))

(defn index-handler
  "For GET requests, always serves index.html from classpath.
   If index.html cannot be found, then a 404 is returned. Any
   method other than GET will return a 405 response."
  [{:keys [request-method]}]
  (if (= request-method :get)
    (if-let [resp (r/resource-response "webui/index.html")]
      (r/content-type resp "text/html")
      (r/not-found))
    (-> (r/response "method not allowed")
        (r/status 405))))
