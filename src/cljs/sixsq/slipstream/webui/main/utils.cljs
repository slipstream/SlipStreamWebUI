(ns sixsq.slipstream.webui.main.utils
  (:require
    [taoensso.timbre :as log]))

(defn add-host-theme
  "Adds a stylesheet link to the head of the document based on the host name
   of the service."
  []
  (let [hostname (.-hostname (.-location js/window))
        href (str "/webui/themes/" hostname "/css/webui.css")
        head (aget (.getElementsByTagName js/document "head") 0)
        link (doto (.createElement js/document "link")
               (.setAttribute "rel" "stylesheet")
               (.setAttribute "href" href))]
    (log/info "adding host theme: " href)
    (.appendChild head link)))
