(def +version+ "3.38-SNAPSHOT")

(set-env!
  :project 'sixsq.slipstream/webui
  :version +version+
  :license {"Apache 2.0" "http://www.apache.org/licenses/LICENSE-2.0.txt"}
  :edition "community"

  :dependencies '[[org.clojure/clojure "1.9.0-beta2"]
                  [sixsq/build-utils "0.1.4" :scope "test"]])

(require '[sixsq.build-fns :refer [merge-defaults
                                   sixsq-nexus-url]])

(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/cljc"}
  :resource-paths #{"resources"}

  :repositories
  #(reduce conj % [["sixsq" {:url (sixsq-nexus-url)}]])

  :dependencies
  #(vec (concat %
                (merge-defaults
                  ['sixsq/default-deps (get-env :version)]
                  '[[org.clojure/clojure]
                    [org.clojure/clojurescript]
                    [org.clojure/core.async]

                    [cljsjs/codemirror "5.24.0-1"]
                    [com.andrewmcveigh/cljs-time]
                    [com.sixsq.slipstream/SlipStreamClientAPI-jar]
                    [com.taoensso/tempura]

                    [expound]

                    [reagent]
                    [re-frame]
                    [re-com]

                    [secretary]

                    ;; boot task and development deps
                    [adzerk/boot-cljs]
                    [adzerk/boot-cljs-repl]
                    [adzerk/boot-reload]
                    [adzerk/boot-test]
                    [binaryage/devtools]
                    [boot-deps]
                    [com.cemerick/piggieback]
                    [crisptrutski/boot-cljs-test]
                    [doo]
                    [onetom/boot-lein-generate]
                    [org.clojure/tools.nrepl]
                    [org.martinklepsch/boot-garden]
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
  '[boot.lein :refer [generate]]
  '[org.martinklepsch.boot-garden :refer [garden]])

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)}
  push {:repo "sixsq"})

(deftask write-version-css
         "Writes a version.css file to the fileset that contains a rule for
          adding the current SlipStream version to the web page footers."
         []
         (let [contents (str "#release-version:after {content: '" +version+ "';}\n")
               tmp (tmp-dir!)]
           (fn middleware [next-handler]
             (fn handler [fileset]
               (empty-dir! tmp)
               (let [out-file (clojure.java.io/file tmp "webui/assets/css/version.css")]
                 (doto out-file
                   clojure.java.io/make-parents
                   (spit contents))
                 (-> fileset
                     (add-resource tmp)
                     commit!
                     next-handler))))))

;;
;; compiler options :pretty-print and :pseudo-names can help with debugging
;; generated javascipt code
;;
(deftask production []
         (task-options! cljs {:optimizations    :advanced
                              :compiler-options {:language-in     :ecmascript5
                                                 :parallel-build  true
                                                 :closure-defines {'sixsq.slipstream.webui.defines/LOGGING_LEVEL "warn"
                                                                   'goog.DEBUG                                   false}}})
         identity)

(deftask optimized-development []
         (task-options! cljs {:optimizations    :advanced
                              :source-map       true
                              :compiler-options {:language-in     :ecmascript5
                                                 :parallel-build  true
                                                 :closure-defines {'sixsq.slipstream.webui.defines/HOST_URL      "https://nuv.la"
                                                                   'goog.DEBUG                                   false}}}
                        reload {:on-jsload 'sixsq.slipstream.webui/init})
         identity)

(deftask development []
         (task-options! cljs {:optimizations    :none
                              :source-map       true
                              :compiler-options {:preloads        '[devtools.preload]
                                                 :language-in     :ecmascript5
                                                 :parallel-build  true
                                                 :closure-defines {'sixsq.slipstream.webui.defines/LOGGING_LEVEL "info"
                                                                   'sixsq.slipstream.webui.defines/HOST_URL      "https://nuv.la"
                                                                   ;'sixsq.slipstream.webui.defines/CONTEXT  ""
                                                                   'goog.DEBUG                                   true}}}
                        reload {:on-jsload 'sixsq.slipstream.webui/init})
         identity)

(deftask build []
         (comp (pom)
               (write-version-css)
               (garden :styles-var 'sixsq.slipstream.webui.styles/base
                       :pretty-print true
                       :output-to "webui/themes/default/css/base.css")
               (sift :add-jar {'cljsjs/codemirror #"cljsjs/codemirror/development/codemirror.css"})
               (sift :move {#"cljsjs/codemirror/development/codemirror.css" "webui/assets/css/codemirror.css"})
               (production)
               (cljs)
               (sift :include #{#".*webui\.out.*" #"webui\.cljs\.edn"
                                #".*authn\.out.*" #"authn\.cljs\.edn"}
                     :invert true)
               (jar)))

(deftask running []
         (set-env! :source-paths #(conj % "test/clj"))
         identity)

(deftask run []
         (comp (running)
               (write-version-css)
               (sift :add-jar {'cljsjs/codemirror #"cljsjs/codemirror/development/codemirror.css"})
               (sift :move {#"cljsjs/codemirror/development/codemirror.css" "webui/assets/css/codemirror.css"})
               (serve :not-found 'sixsq.slipstream.webui.run/index-handler)
               (watch)
               (garden :styles-var 'sixsq.slipstream.webui.styles/base
                       :pretty-print true
                       :output-to "webui/themes/default/css/base.css")
               (cljs-repl)
               (reload)
               (speak)
               (cljs)))

(deftask dev
         "Simple alias to run application in development mode"
         []
         (comp (development)
               #_(optimized-development)
               (run)))

(deftask testing []
         (set-env! :source-paths #(conj % "test/cljs"))
         identity)

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

;; FIXME: Remove :process-shim flag when possible.
;; See https://github.com/bensu/doo/pull/141
(deftask test []
         (comp (testing)
               (test-cljs :js-env :phantom
                          :cljs-opts {:process-shim false}
                          :exit? true)))

(deftask deps [])

;; FIXME: Remove :process-shim flag when possible.
;; See https://github.com/bensu/doo/pull/141
(deftask auto-test []
         (comp (testing)
               (watch)
               (test-cljs :js-env :phantom
                          :cljs-opts {:process-shim false})))

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
