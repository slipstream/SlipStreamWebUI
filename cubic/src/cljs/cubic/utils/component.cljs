(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.utils.component)

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
