(ns sixsq.slipstream.webui.electron.main)

(def electron (js/require "electron"))
(def app (.-app electron))
(def BrowserWindow (.-BrowserWindow electron))

(def main-window (atom nil))

(goog-define devtools? false)
(goog-define none? false)


(defn load-page
  "When compiling with `:none` the compiled JS that calls .loadURL is
  in a different place than it would be when compiling with optimizations
  that produce a single artifact (`:whitespace, :simple, :advanced`).

  Because of this we need to dispatch the loading based on the used
  optimizations, for this we defined `none?` above that we can override
  at compile time using the `:clojure-defines` compiler option."
  [^js/electron.BrowserWindow window]
  (if none?
    (.loadURL window (str "file://" js/__dirname "/../../../../electron.html"))
    (.loadURL window (str "file://" js/__dirname "/public/electron.html"))))


(defn mk-window [w h frame? show?]
  (BrowserWindow. #js {:width w, :height h, :frame frame?, :show show?}))


(defn init-browser []
  (reset! main-window (mk-window 1000 650 true true))
  (load-page @main-window)
  (when devtools? (.openDevTools ^js/electron.BrowserWindow @main-window))
  (.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil)))


(defn ^:export init []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin") (.quit app)))
  (.on app "ready" init-browser)
  (set! *main-cli-fn* (constantly nil)))

(init)
