(ns sixsq.slipstream.webui-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require [cljs.test :as t]
            [sixsq.slipstream.webui :as webui]))

(deftest placeholder []
  (is (= 1 1)))
