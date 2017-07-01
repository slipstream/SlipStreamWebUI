(ns sixsq.slipstream.webui.widget.breadcrumbs.utils-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require [cljs.test :refer [are]]
            [sixsq.slipstream.webui.widget.breadcrumbs.utils :as t]))

(deftest check-breadcrumbs->url
  (are [expected input] (= expected (t/breadcrumbs->url input))
                        nil nil
                        nil []
                        nil 10
                        "a" ["a"]
                        "a/b" ["a" "b"]
                        "a/10/b" ["a" 10 "b"]))

