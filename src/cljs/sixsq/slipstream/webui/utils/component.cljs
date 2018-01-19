(ns sixsq.slipstream.webui.utils.component)

(defn callback
  "Create a UI callback function that has the standard signature f(evt data),
   converts the javascript data object to clojurescript, and then calls the
   provided function with the reformatted data."
  [kw f]
  (fn [evt data]
    (-> data
        (js->clj :keywordize-keys true)
        kw
        f)))
