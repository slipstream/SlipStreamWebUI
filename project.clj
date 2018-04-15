(def +version+ "3.50-SNAPSHOT")

(def package-json-template
"
{
    \"name\": \"CUBIC\",
    \"version\": \"3.49-SNAPSHOT\",
    \"main\": \"resources/electron-main.js\",
    \"devDependencies\": {
        \"electron\": \"^1.8.4\",
        \"electron-packager\": \"^12.0.0\",
        \"electron-installer-dmg\": \"0.2.1\"
    }
}
")

(defproject com.sixsq.slipstream/SlipStreamWebUI "3.50-SNAPSHOT"

  :description "Web Browser User Interface"

  :url "https://github.com/slipstream/SlipStreamWebUI"

  :license {:name         "Apache 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}

  :plugins [[lein-parent "0.3.2"]
            [lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.8"]
            [lein-unpack-resources "0.1.1"]
            [pdok/lein-filegen "0.1.0"]
            [lein-resource "16.9.1"]]

  :parent-project {:coords  [sixsq/slipstream-parent "5.1.1"]
                   :inherit [:min-lein-version
                             :managed-dependencies
                             :repositories
                             :deploy-repositories]}

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"
                                    "test/js"
                                    "resources/public/css/version.css"
                                    "resources/public/css/codemirror.css"
                                    "resources/electron-main.js"
                                    "resources/public/js/electron-renderer.js"
                                    "resources/public/js/electron-renderer.js.map"
                                    "resources/public/js/electron-renderer-out"
                                    "resources/public/js/electron-release"]

  :auto-clean false

  :prep-tasks []

  :pom-location "target/"

  :unpack-resources {:resource [cljsjs/codemirror "5.31.0-0"] :extract-path "target/cljsjs/codemirror"}

  :filegen [{:data        ["#release-version:after {content: '" ~+version+ "';}\n"]
             :template-fn #(apply str %)
             :target      "target/version.css"}
            {:data        [~package-json-template ~+version+]
             :template-fn #(apply format %)
             :target      "./package.json"}]

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
                 [com.taoensso/timbre]
                 [cljsjs/codemirror "5.31.0-0"]
                 [com.sixsq.slipstream/SlipStreamClojureAPI-cimi ~+version+]
                 [com.taoensso/tempura]
                 [cljsjs/semantic-ui-react]
                 [cljsjs/moment]
                 [cljsjs/react-date-range]
                 [funcool/promesa]
                 [com.taoensso/encore]                      ;fix conflict, needed indirectly
                 [camel-snake-kebab]
                 [cljsjs/vega-embed]
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
                                           day8.re-frame-10x.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_"         true
                                           sixsq.slipstream.webui.utils.defines/HOST_URL "https://localhost"
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
                    :output-dir    "target/test/webui/out"
                    :optimizations :whitespace}}

    ;;
    ;; electron UI builds
    ;;

    {:source-paths ["src/cljs"]
     :id           "electron-dev"
     :compiler     {:output-to      "resources/main.js"
                    :output-dir     "resources/public/js/electron-dev"
                    :optimizations  :simple
                    :pretty-print   true
                    :cache-analysis true}}
    #_{:source-paths ["src/cljs"]
       :id           "frontend-dev"
       :compiler     {:output-to      "resources/public/js/electron-ui-core.js"
                      :output-dir     "resources/public/js/electron-ui-out"
                      :source-map     true
                      :asset-path     "js/electron-ui-out"
                      :optimizations  :none
                      :cache-analysis true
                      :main           "dev.core"}}
    {:source-paths ["src/cljs"]
     :id           "electron-release"
     :compiler     {:output-to       "resources/electron-main.js"
                    :output-dir      "resources/public/js/electron-release"
                    :optimizations   :simple #_:advanced    ;; FIXME: advanced doesn't work
                    :pretty-print    true
                    :cache-analysis  true
                    ;:infer-externs  true
                    :main            "sixsq.slipstream.webui.electron.main"
                    :closure-defines {sixsq.slipstream.webui.electron.main/devtools? true
                                      goog.DEBUG                                     true}}}
    {:source-paths ["src/cljs"]
     :id           "frontend-release"

     :compiler     {:output-to       "resources/public/js/electron-renderer.js"
                    :output-dir      "resources/public/js/electron-renderer-out"
                    :source-map      "resources/public/js/electron-renderer.js.map"
                    :optimizations   :simple #_:advanced    ;; FIXME: advanced doesn't work
                    :cache-analysis  true
                    ;:infer-externs  true
                    :main            "sixsq.slipstream.webui.electron.renderer"
                    :closure-defines {sixsq.slipstream.webui.utils.defines/HOST_URL "https://nuv.la"
                                      sixsq.slipstream.webui.utils.defines/CONTEXT  ""
                                      goog.DEBUG                                    true}}}

    ]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools]
                   [day8.re-frame/re-frame-10x]
                   [ring]
                   [ring/ring-defaults]
                   [compojure]]
    :figwheel     {:server-port  3000
                   :ring-handler sixsq.slipstream.webui.dev_server/http-handler}}}

  :aliases {"prepare"   ["do" ["filegen"] ["unpack-resources"] ["resource"]]
            "dev"       ["do" "prepare" ["figwheel" "dev"]]
            "install"   ["do" "prepare" ["cljsbuild" "once" "prod"] ["install"]]
            "test-auto" ["doo" "nashorn" "test"]
            "test"      ["test-auto" "once"]
            "electron"  ["do" ["clean"] "prepare"
                         ["cljsbuild" "once" "electron-release"]
                         ["cljsbuild" "once" "frontend-release"]]})
