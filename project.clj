(def +version+ "3.44")

;; FIXME: Provide HTTPS access to Nexus.
(require 'cemerick.pomegranate.aether)
(cemerick.pomegranate.aether/register-wagon-factory!
  "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))

(defproject
  sixsq.slipstream/webui
  "3.44"
  :license
  {"Apache 2.0" "http://www.apache.org/licenses/LICENSE-2.0.txt"}

  :plugins [[lein-parent "0.3.2"]
            [lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-unpack-resources "0.1.1"]
            [pdok/lein-filegen "0.1.0"]
            [lein-resource "16.9.1"]
            [lein-doo "0.1.8"]]

  :parent-project {:coords  [com.sixsq.slipstream/parent "3.44"]
                   :inherit [:min-lein-version :managed-dependencies :repositories :deploy-repositories]}

  :clean-targets ^{:protect false} ["target"
                                    "resources/public/webui/assets/js"
                                    "resources/public/authn/assets/js"
                                    "resources/public/legacy/assets/js"
                                    "resources/public/webui/js/webui.js"
                                    "resources/public/authn/js/authn.js"
                                    "resources/public/legacy/js/legacy.js"
                                    "resources/public/webui/assets/css/version.css"
                                    "resources/public/webui/assets/css/codemirror.css"]

  :auto-clean false

  :prep-tasks []

  :source-paths ["src/cljs" "src/cljc"]

  :pom-location "target/"

  :resource {:resource-paths
             [["target/cljsjs/codemirror/cljsjs/codemirror/development/codemirror.css"
               {:target-path "resources/public/webui/assets/css/codemirror.css"}]
              ["target/version.css"
               {:target-path "resources/public/webui/assets/css/version.css"}]]}

  :cljsbuild
  {:builds [
            {:id           "dev-webui"
             :source-paths ["src/cljs" "src/cljc"]
             :compiler     {:main                 sixsq.slipstream.webui
                            :output-to            "resources/public/webui/js/webui.js"
                            :output-dir           "resources/public/webui/assets/js/"
                            :asset-path           "assets/js"
                            :optimizations        :none
                            :source-map           true
                            :source-map-timestamp true
                            :preloads             [devtools.preload]
                            :parallel-build       true
                            :closure-defines      {sixsq.slipstream.webui.defines/LOGGING_LEVEL "info"
                                                   sixsq.slipstream.webui.defines/HOST_URL      "https://nuv.la"
                                                   ;'sixsq.slipstream.webui.defines/CONTEXT     ""
                                                   goog.DEBUG                                   true}
                            :external-config      {:devtools/config {:features-to-install :all}}
                            }
             :figwheel     {:on-jsload "sixsq.slipstream.webui/init"
                            :open-urls ["http://localhost:3000/webui/index.html"]}}

            {:id           "prod-webui"
             :source-paths ["src/cljs" "src/cljc"]
             :compiler     {:main            sixsq.slipstream.webui
                            :output-to       "resources/public/webui/js/webui.js"
                            :output-dir      "target/webui/assets/js/"
                            :optimizations   :advanced
                            :parallel-build  true
                            :closure-defines {sixsq.slipstream.webui.defines/LOGGING_LEVEL "warn"
                                              goog.DEBUG                                   false}}}
            {:id           "prod-authn"
             :source-paths ["src/cljs" "src/cljc"]
             :compiler     {:main            sixsq.slipstream.authn
                            :output-to       "resources/public/authn/js/authn.js"
                            :output-dir      "target/authn/assets/js/"
                            :optimizations   :advanced
                            :parallel-build  true
                            :closure-defines {sixsq.slipstream.webui.defines/LOGGING_LEVEL "warn"
                                              goog.DEBUG                                   false}}}
            {:id           "test"
             :source-paths ["src/cljs" "src/cljc" "test/cljs"]
             :compiler     {:main          sixsq.slipstream.webui.runner
                            :output-to     "target/test/webui/webui-test.js"
                            :output-dir    "target/test/webui/"
                            :optimizations :none}}
            {:id           "dev-legacy"
             :source-paths ["src/cljs"]
             :compiler     {:main            sixsq.slipstream.legacy.components
                            :output-to       "resources/public/legacy/js/legacy.js"
                            :output-dir      "resources/public/legacy/assets/js"
                            :asset-path      "assets/js"
                            :optimizations   :none
                            :source-map      true
                            :preloads        [devtools.preload]
                            :parallel-build  true
                            :external-config {:devtools/config {:features-to-install :all}}}
             :figwheel     {:on-jsload "sixsq.slipstream.legacy.components/init"
                            :open-urls ["http://localhost:3000/legacy/index.html"]}}
            {:id           "prod-legacy"
             :source-paths ["src/cljs" "src/cljc"]
             :compiler     {:main            sixsq.slipstream.legacy.components
                            :output-to       "resources/public/legacy/js/legacy.js"
                            :output-dir      "target/legacy/assets/js"
                            :optimizations   :advanced
                            :parallel-build  true}}
            ]}

  :figwheel {:server-port 3000}

  :unpack-resources {:resource [cljsjs/codemirror "5.24.0-1"] :extract-path "target/cljsjs/codemirror"}

  :filegen [{:data        ["#release-version:after {content: '" ~+version+ "';}\n"]
             :template-fn #(apply str %)
             :target      "target/version.css"}]

  :dependencies
  [[org.clojure/clojure]
   [org.clojure/clojurescript]
   [org.clojure/core.async]

   [com.andrewmcveigh/cljs-time]
   [com.sixsq.slipstream/SlipStreamClientAPI-jar]
   [com.taoensso/tempura]

   [expound]

   [reagent]
   [re-frame]
   [re-com]

   [secretary]

   [cljsjs/codemirror "5.24.0-1"]

   [soda-ash]
   [funcool/promesa]

   [cljsjs/moment]
   [cljsjs/react-date-range]]

  :profiles {:dev {:dependencies [[binaryage/devtools]]}}

  :aliases {"prepare"    ["do" ["filegen"] ["unpack-resources"] ["resource"]]
            "dev-webui"  ["do" ["prepare"] ["figwheel" "dev-webui"]]
            "dev-legacy" ["figwheel" "dev-legacy"]
            "prod"       ["do" ["prepare"] ["cljsbuild" "once" "prod-webui" "prod-authn" "prod-legacy"] ["install"]]
            "test-auto"  ["doo" "phantom" "test"]
            "test"       ["test-auto" "once"]})
