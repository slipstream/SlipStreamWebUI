(ns cubic.config
  (:require
    [cubic.history.utils :as utils]))

(def debug?
  ^boolean goog.DEBUG)

(def context "/cubic")

(def path-prefix (delay (str (utils/host-url) context)))
