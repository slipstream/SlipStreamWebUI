(ns sixsq.slipstream.webui.panel.cimi.utils-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require
    [cljs.test]
    [sixsq.slipstream.webui.panel.cimi.utils :as t]))

(def cep {:baseURI             "https://slipstream.example.org"
          :events              {:href "events"}
          :collectionTemplates {:href "collection-template"}
          :others              {:href #{:href}}
          :badForms            {}})

(deftest check-cep-maps
  (let [expected-href {:events              "events"
                       :collectionTemplates "collection-template"}
        expected-key (into {} (map (juxt second first) expected-href))]
    (is (= expected-href (t/collection-href-map cep)))
    (is (= expected-key (t/collection-key-map cep)))))
