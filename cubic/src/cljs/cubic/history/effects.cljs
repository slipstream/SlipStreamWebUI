(ns
  ^{:copyright "Copyright 2017, SixSq Sàrl"
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.history.effects
  (:require
    [re-frame.core :refer [reg-fx]]
    [cubic.history.utils :as utils]))


(reg-fx
  ::initialize
  (fn [[path-prefix]]
    (utils/initialize path-prefix)
    (utils/start path-prefix)))


(reg-fx
  ::navigate
  (fn [[url]]
    (utils/navigate url)))

(reg-fx
  ::navigate-js-location
  (fn [[url]]
    (.replace js/location url)))
