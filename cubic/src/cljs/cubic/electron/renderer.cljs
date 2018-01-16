(ns cubic.electron.renderer
  (:require
   [reagent.core :as reagent :refer [atom]]
   [clojure.string :as str]))

(set! *warn-on-infer* true)

(def join-lines (partial str/join "\n"))

(enable-console-print!)

(defonce state        (atom 0))
(defonce shell-result (atom ""))
(defonce command      (atom ""))

(defonce ^js/child_process  proc (js/require "child_process"))

(defn append-to-out [out]
  (swap! shell-result str out))

(defn run-process []
  (when-not (empty? @command)
    (.log js/console "Running command:" @command)
    (let [[cmd & args] (str/split @command #"\s")
          js-args (clj->js (or args []))
          ^js/ChildProcess p (.spawn proc cmd js-args)]
      (.on p "error" (comp append-to-out
                           #(str % "\n")))
      (.on (.-stderr p) "data" append-to-out)
      (.on (.-stdout p) "data" append-to-out))
    (reset! command "")))

(defn root-component []
  [:div
   [:div.logos
    [:img.electron {:src "img/sixsq-logo.png"}]]
   [:pre "Versions:"
    [:p (str "Node     " js/process.version)]
    [:p (str "Electron " ((js->clj js/process.versions) "electron"))]
    [:p (str "Chromium " ((js->clj js/process.versions) "chrome"))]]
   [:button
    {:on-click #(swap! state inc)}
    (str "Clicked " @state " times")]
   [:div
    [:form
     {:on-submit (fn [^js/Event e]
                   (.preventDefault e)
                   (run-process))}
     [:input#command
      {:type :text
       :on-change (fn [^js/Event e]
                    (reset! command
                            ^js/String (.-value (.-target e))))
       :value @command
       :placeholder "type in shell command"}]]]
   [:pre (join-lines (take 100 (reverse (str/split-lines @shell-result))))]])

(defn ^:export init []
  (.log js/console "Starting renderer...")
  (reagent/render
   [root-component]
   (js/document.getElementById "app-container")))
