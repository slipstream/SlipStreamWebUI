(set-env!
  :source-paths #{"src/cljs"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojurescript "1.9.293"]
                  [reagent "0.6.0"]

                  [re-frame "0.8.0"]
                  [binaryage/devtools "0.8.3"]
                  [secretary "1.2.3"]

                  [org.clojure/core.async "0.2.395"]
                  [reagent-forms "0.5.28"]
                  [reagent-reforms "0.4.3"]
                  [org.clojars.frozenlock/reagent-modals "0.2.6"]
                  [cljsjs/react-bootstrap "0.30.6-0"]
                  [com.sixsq.slipstream/SlipStreamClientAPI-jar "3.16-SNAPSHOT"]

                  [adzerk/boot-cljs "1.7.228-2" :scope "test"]
                  [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                  [adzerk/boot-reload "0.4.13" :scope "test"]
                  [pandeiro/boot-http "0.7.6" :scope "test"]
                  [com.cemerick/piggieback "0.2.1" :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                  [weasel "0.7.0" :scope "test"]
                  [crisptrutski/boot-cljs-test "0.2.2" :scope "test"]
                  [boot-deps "0.1.6" :scope "test"]])

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload :refer [reload]]
  '[pandeiro.boot-http :refer [serve]]
  '[crisptrutski.boot-cljs-test :refer [test-cljs]]
  '[boot-deps :refer [ancient]])

(deftask build []
         (comp (speak)
               (cljs)))

(deftask run []
         (comp (serve)
               (watch)
               (cljs-repl)
               (reload)
               (build)))

(deftask production []
         (task-options! cljs {:optimizations :advanced})
         identity)

(deftask development []
         (task-options! cljs {:optimizations :none :source-map true}
                        reload {:on-jsload 'sixsq.slipstream.webui/init})
         identity)

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

(deftask auto-test []
         (comp (testing)
               (watch)
               (test-cljs :js-env :phantom)))
