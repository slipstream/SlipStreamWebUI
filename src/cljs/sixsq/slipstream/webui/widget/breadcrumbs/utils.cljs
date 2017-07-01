(ns sixsq.slipstream.webui.widget.breadcrumbs.utils
  (:require
    [clojure.string :as str]))

(defn breadcrumbs->url
  "Converts a sequence of crumbs to a URL by separating the values with
   slashes. Returns nil if the argument is not a sequence or is empty."
  [crumbs]
  (if (seq crumbs)
    (str/join "/" crumbs)))
