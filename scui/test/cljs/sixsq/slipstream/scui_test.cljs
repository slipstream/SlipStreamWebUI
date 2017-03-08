(ns sixsq.slipstream.scui-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require [cljs.test :as t]
            [sixsq.slipstream.scui :as scui]))

(deftest placeholder []
  (is (= 1 1)))
