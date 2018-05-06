(ns sixsq.slipstream.webui.utils.general-test
  (:require
    [cljs.test :refer-macros [are deftest is run-tests testing]]
    [sixsq.slipstream.webui.utils.general :as t]))


(deftest check-str->int
  (are [s expected] (= expected (t/str->int s))
                    nil nil
                    true nil
                    1.2 nil
                    1 nil
                    "0a" nil
                    "+1" nil
                    "-1" -1
                    "0" 0
                    "1" 1
                    "-10" -10
                    "10" 10))


(deftest check-parse-resource-path
  (are [path expected] (= expected (t/parse-resource-path path))
                       nil []
                       "" []
                       "a" ["a"]
                       "a/b" ["a" "b"]
                       "a/b/c" ["a" "b" "c"]
                       "a//c" ["a" "c"]
                       true ["true"]))


(deftest check-truncate
  (let [v "abcdefghijklmnopqrstuvwxyz0123456789"]
    (is (= (t/truncate v)
           (t/truncate v t/default-truncate-length)
           (t/truncate v t/default-truncate-length t/ellipsis)))
    (is (= "abc!" (t/truncate v 3 "!")))))


(deftest check-json-edn-conversions
  (let [orig {:alpha 1, :beta "two", :gamma "3.0", :delta false, :zeta {:a 1, :b 2}}
        json (t/edn->json orig)
        reread (t/json->edn json)]
    (is (string? json))
    (is (= orig reread))))


(deftest check-random-element-id
  (let [test-size (* 2 t/default-random-id-size)]

    ;; default, non-default sizes produce identifiers with correct number of characters
    (is (= t/default-random-id-size (count (t/random-element-id))))
    (is (= test-size (count (t/random-element-id test-size))))

    ;; all calls produce unique identifiers
    (let [ids (repeatedly 20 t/random-element-id)]
      (is (= (count ids) (count (set ids)))))))
