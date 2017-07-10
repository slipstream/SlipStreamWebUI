(ns sixsq.slipstream.webui.resource)

(defmulti render
          "Multimethod that accepts path and query parameters, then routes
           to the method that will render the resource. The multimethod
           dispatches on the first element of the path."
          (fn [path query-params]
            (first path)))
