(ns sixsq.slipstream.webui.utils-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require [cljs.test :refer [are]]
            [sixsq.slipstream.webui.utils :as t]))

(deftest check-str->int
  (are [expected input] (= expected (t/str->int input))
                        0 "0"
                        1 "1"
                        -1 "-1"
                        10 "10")
  (are [input] (nil? (t/str->int input))
               nil
               ""
               "-"
               "0xf"
               "010"
               " -10"
               " 10"
               "not-a-number"
               100
               true))

(deftest check-parse-resource-path
  (are [expected path] (= expected (t/parse-resource-path path))
                       [] nil
                       [] ""
                       ["a"] "a"
                       ["a"] "/a"
                       ["a"] "a/"
                       ["a" "b"] "a/b"
                       ["a" "b"] "a//b"
                       ["a" "b"] "a/\t/b"))

(deftest check-truncate
  (are [expected input size] (= expected (t/truncate input size))
                             "mickey" "mickey" 10
                             "mickey" "mickey" 6
                             "mick" "mickey" 4)
  (let [suffix "…"]
    (are [expected input size] (= expected (t/truncate input size suffix))
                               "mickey" "mickey" 10
                               "mickey" "mickey" 6
                               (str "mick" suffix) "mickey" 4)))
