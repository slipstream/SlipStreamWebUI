(ns sixsq.slipstream.webui-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require [cljs.test :as t]
            [sixsq.slipstream.webui :as webui]))

(deftest test-arithmetic []
  (is (= (+ 0.1 0.2) 0.3) "Something foul is a float."))
