(set-env!
  :source-paths #{"src/cljs"}
  :resource-paths #{"html"}

  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [org.clojure/clojurescript "1.9.293"]
                  [org.clojure/core.async "0.2.395"]
                  [reagent "0.6.0"]
                  [cljsjs/react-bootstrap "0.30.6-0"]
                  [com.sixsq.slipstream/SlipStreamClientAPI-jar "3.16-SNAPSHOT"]
                  [adzerk/boot-cljs "1.7.228-2" :scope "test"]
                  [adzerk/boot-reload "0.4.13" :scope "test"]
                  [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                  [pandeiro/boot-http "0.7.6" :scope "test"]
                  [com.cemerick/piggieback "0.2.1" :scope "test"]
                  [weasel "0.7.0" :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                  [boot-deps "0.1.6"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[pandeiro.boot-http :refer [serve]]
         '[boot-deps :refer [ancient]])

(deftask dev
         "launch interactive development environment"
         []
         (comp
           (serve :dir "target")
           (watch)
           (reload)
           (cljs-repl)
           (cljs)
           (target :dir #{"target"})))

