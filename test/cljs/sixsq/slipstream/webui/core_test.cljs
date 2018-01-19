(ns sixsq.slipstream.webui.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [sixsq.slipstream.webui.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
