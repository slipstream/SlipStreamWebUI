(def +version+ "3.44-SNAPSHOT")

;; FIXME: Provide HTTPS access to Nexus.
(require 'cemerick.pomegranate.aether)
(cemerick.pomegranate.aether/register-wagon-factory!
  "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))

(defproject
  sixsq.slipstream/webui
  "3.44-SNAPSHOT"
  :license
  {"Apache 2.0" "http://www.apache.org/licenses/LICENSE-2.0.txt"}

  :plugins [[lein-parent "0.3.2"]
            [lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.8"]
            [lein-unpack-resources "0.1.1"]
            [pdok/lein-filegen "0.1.0"]
            [lein-resource "16.9.1"]]

  :parent-project {:coords  [com.sixsq.slipstream/parent "3.44-SNAPSHOT"]
                   :inherit [:min-lein-version :managed-dependencies :repositories :deploy-repositories]}

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"
                                    "test/js"
                                    "resources/public/css/version.css"
                                    "resources/public/css/codemirror.css"]

  :auto-clean false

  :prep-tasks []

  :pom-location "target/"

  :unpack-resources {:resource [cljsjs/codemirror "5.24.0-1"] :extract-path "target/cljsjs/codemirror"}

  :filegen [{:data        ["#release-version:after {content: '" ~+version+ "';}\n"]
             :template-fn #(apply str %)
             :target      "target/version.css"}]

  :resource {:resource-paths
             [["target/cljsjs/codemirror/cljsjs/codemirror/development/codemirror.css"
               {:target-path "resources/public/css/codemirror.css"}]
              ["target/version.css"
               {:target-path "resources/public/css/version.css"}]]}


  :dependencies [[org.clojure/clojure]
                 [org.clojure/clojurescript]
                 [reagent]

                 [re-frame]
                 [secretary]
                 [expound]
                 [com.taoensso/timbre "4.10.0"]
                 [cljsjs/codemirror "5.24.0-1"]
                 [com.sixsq.slipstream/SlipStreamClientAPI-jar]
                 [com.taoensso/tempura]
                 [cljsjs/semantic-ui-react "0.77.2-0" :exclusions [cljsjs/react]] ;;TODO this is a local version waiting https://github.com/cljsjs/packages/pull/1461
                 [cljsjs/moment]
                 [cljsjs/react-date-range]
                 [funcool/promesa]
                 ]

  :source-paths ["src/clj"]

  :test-paths ["test/cljs"]

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs" "test/clj"]
     :figwheel     {:on-jsload "sixsq.slipstream.webui.core/mount-root"}
     :compiler     {:main                 sixsq.slipstream.webui.core
                    :output-to            "resources/public/js/webui.js"
                    :output-dir           "resources/public/js/out"
                    :asset-path           "/js/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame.trace.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_"         true
                                           sixsq.slipstream.webui.utils.defines/HOST_URL "https://nuv.la"
                                           ;'sixsq.slipstream.webui.utils.defines/CONTEXT     ""
                                           goog.DEBUG                                    true}
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}
    {:id           "prod"
     :source-paths ["src/cljs"]
     :compiler     {:main            sixsq.slipstream.webui.core
                    :output-to       "resources/public/js/webui.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          sixsq.slipstream.webui.runner
                    :output-to     "target/test/webui/webui-test.js"
                    :output-dir    "target/test.webui/"
                    :optimizations :none}}
    ]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools]
                   [day8.re-frame/trace "0.1.14"]
                   [ring]
                   [ring/ring-defaults]
                   [compojure]]
    :figwheel     {:server-port 3000
                   :ring-handler sixsq.slipstream.webui.dev_server/http-handler}
    }
   }

  :aliases {"prepare"   ["do" ["filegen"] ["unpack-resources"] ["resource"]]
            "dev"       ["do" "prepare" ["figwheel" "dev"]]
            "prod"      ["do" "prepare" ["cljsbuild" "once" "prod"] ["install"]]
            "test-auto" ["doo" "phantom" "test"]
            "test"      ["test-auto" "once"]}
  )
