(defproject cubic "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.2"]
                 [secretary "1.2.3"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-ancient "0.6.14"]]

  :min-lein-version "2.7.1"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.8"]
                   [cljsjs/d3 "4.12.0-0"]
                   [cljsjs/codemirror "5.31.0-0"]
                   [com.sixsq.slipstream/SlipStreamClientAPI-jar "3.42-SNAPSHOT"]
                   [com.taoensso/tempura "1.1.2"]
                   [day8.re-frame/trace "0.1.14"]
                   [expound "0.4.0"]
                   [reagent "0.7.0"]
                   [re-frame "0.10.2"]
                   [secretary "1.2.3"]
                   [soda-ash "0.76.0"]
                   [ring "1.6.3"]
                   [ring/ring-defaults "0.3.1"]
                   [compojure "1.6.0"]
                   ]

    :plugins      [[lein-figwheel "0.5.13"]
                   [lein-doo "0.1.8"]]

    :figwheel     {:ring-handler cubic.dev_server/http-handler}
    }}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs" "test/clj"]
     :figwheel     {:on-jsload "cubic.core/mount-root"}
     :compiler     {:main                 cubic.core
                    :output-to            "resources/public/js/compiled/cubic.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "/js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame.trace.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true
                                           cubic.utils.defines/HOST_URL      "https://nuv.la"
                                           ;'cubic.utils.defines/CONTEXT     ""
                                           goog.DEBUG                                   true}
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            cubic.core
                    :output-to       "resources/public/js/compiled/cubic.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          cubic.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}
    ]}

  )
