(def +version+ "3.30-SNAPSHOT")

(set-env!
  :project 'sixsq.slipstream/webui
  :version +version+
  :license {"Apache 2.0" "http://www.apache.org/licenses/LICENSE-2.0.txt"}
  :edition "community"

  :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                  [sixsq/build-utils "0.1.4" :scope "test"]])

(require '[sixsq.build-fns :refer [merge-defaults
                                   sixsq-nexus-url]])

(set-env!
  :source-paths #{"src/cljs"}
  :resource-paths #{"resources"}

  :repositories
  #(reduce conj % [["sixsq" {:url (sixsq-nexus-url)}]])

  :dependencies
  #(vec (concat %
                (merge-defaults
                  ['sixsq/default-deps (get-env :version)]
                  '[[org.clojure/clojure]
                    [org.clojure/clojurescript]

                    [binaryage/devtools]
                    [com.sixsq.slipstream/SlipStreamClientAPI-jar]
                    [com.taoensso/tempura]

                    [secretary]

                    [org.clojure/core.async]

                    [reagent]
                    [re-frame]
                    [re-com]
                    [com.andrewmcveigh/cljs-time "0.5.0"]
                    
                    [adzerk/boot-cljs]
                    [adzerk/boot-cljs-repl]
                    [adzerk/boot-reload]
                    [adzerk/boot-test]
                    [boot-deps]
                    [com.cemerick/piggieback]
                    [crisptrutski/boot-cljs-test]
                    [doo]
                    [onetom/boot-lein-generate]
                    [org.clojure/tools.nrepl]
                    [pandeiro/boot-http]
                    [tolitius/boot-check]
                    [weasel]]))))

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-test :refer [test]]
  '[adzerk.boot-reload :refer [reload]]
  '[crisptrutski.boot-cljs-test :refer [test-cljs]]
  '[pandeiro.boot-http :refer [serve]]
  '[tolitius.boot-check :refer [with-yagni
                                with-eastwood
                                with-kibit
                                with-bikeshed]]
  '[boot-deps :refer [ancient]]
  '[boot.lein :refer [generate]])

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)}
  push {:repo "sixsq"})

;;
;; compiler options :pretty-print and :pseudo-names can help with debugging
;; generated javascipt code
;;
(deftask production []
         (task-options! cljs {:optimizations    :advanced
                              :compiler-options {:language-in     :ecmascript5
                                                 :closure-defines {'sixsq.slipstream.webui/DEV false
                                                                   'goog.DEBUG                 false}}})
         identity)

(deftask development []
         (task-options! cljs {:optimizations    :none
                              :source-map       true
                              :compiler-options {:language-in     :ecmascript5
                                                 :closure-defines {'sixsq.slipstream.webui/DEV      true
                                                                   'sixsq.slipstream.webui/HOST_URL "https://nuv.la"
                                                                   ;'sixsq.slipstream.webui/CONTEXT  ""
                                                                   'goog.DEBUG                      true}}}
                        reload {:on-jsload 'sixsq.slipstream.webui/init})
         identity)

(deftask build []
         (comp (pom)
               (production)
               (cljs)
               (sift :include #{#".*webui\.out.*" #"webui\.cljs\.edn"}
                     :invert true)
               (jar)))

(deftask running []
         (set-env! :source-paths #(conj % "test/clj"))
         identity)

(deftask run []
         (comp (running)
               (serve :not-found 'sixsq.slipstream.webui.run/index-handler)
               (watch)
               (cljs-repl)
               (reload)
               (speak)
               (cljs)))

(deftask dev
         "Simple alias to run application in development mode"
         []
         (comp (development)
               (run)))

(deftask testing []
         (set-env! :source-paths #(conj % "test/cljs"))
         identity)

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test []
         (comp (testing)
               (test-cljs :js-env :phantom
                          :exit? true)))

(deftask deps [])

(deftask auto-test []
         (comp (testing)
               (watch)
               (test-cljs :js-env :phantom)))

(deftask mvn-test
         "run all tests of project"
         []
         (test))

(deftask mvn-build
         "build full project through maven"
         []
         (comp
           (build)
           (install)
           (if (= "true" (System/getenv "BOOT_PUSH"))
             (push)
             identity)))
