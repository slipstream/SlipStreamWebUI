(ns sixsq.slipstream.webui.components.editor
  (:require
    [re-com.core :refer [label]]
    [re-com.util :refer [deref-or-value]]
    [re-com.validate :refer-macros [validate-args-macro]]
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.javascript]
    [taoensso.timbre :as log]
    [clojure.string :as str]
    [cljs.spec.alpha :as s]))

(def json-options {:lineNumbers true
                   :mode        {:name "javascript", :json true}})

(def default-args {:options {}
                   :change-on-blur? true})

(defn random-id
  "Random six character string that can be used to generate unique
   identifiers."
  []
  (let [rand-alphanum #(rand-nth (vec "abcdefghijklmnopqrstuvwxyz0123456789"))]
    (str/join "" (take 6 (repeatedly rand-alphanum)))))

(defn callback-with-text
  "Creates a callback function that will extract the current value of the
   editor and pass this to the given function."
  [callback]
  (fn [cm & _]
    (let [text (.getValue cm)]
      (callback text))))

(defn editor-inner
  "Function that acts as the reagent component for the editor. This should
   only be used via the public component function."
  [{:keys [options] :as args}]
  (let [cm (atom nil)
        text-area-id (str "editor-" (random-id))
        options (clj->js options)
        update (fn [comp old-argv]
                 (let [{:keys [text] :as args} (reagent/props comp)]
                   (.setValue @cm (or (deref-or-value text) ""))))]

    (reagent/create-class
      {:reagent-render       (fn [args]
                               [:textarea {:id text-area-id}])

       :component-did-mount  (fn [comp]
                               (let [{:keys [text on-change change-on-blur?] :as args} (reagent/props comp)]
                                 (let [text-area (.getElementById js/document text-area-id)
                                       cm-instance (.fromTextArea js/CodeMirror text-area options)]
                                   (when on-change
                                     (if change-on-blur?
                                       (.on cm-instance "blur" (callback-with-text on-change))
                                       (.on cm-instance "change" (callback-with-text on-change))))
                                   (reset! cm cm-instance))
                                 (update comp nil)))

       :component-did-update update

       :display-name         "codemirror-editor"})))

;;
;; public component
;;

(def editor-args-desc
  [{:name :text, :required true, :type "nil | string | atom"}
   {:name :options, :required false, :type "options map", :validate-fn map?}
   {:name :on-change, :required true, :type "update fn", :validate-fn fn?}
   {:name :change-on-blur?, :required false, :type "boolean", :validate-fn boolean?}])

(defn editor
  "Provides an editor component based on Codemirror. To use modes other than
   javascript, you will have to require the mode explicitly in your code."
  [& {:as args}]
  {:pre [(validate-args-macro editor-args-desc args "editor")]}
  [editor-inner (merge default-args args)])

(defn json-editor
  "A convenience function to setup the CodeMirror editor component for JSON."
  [& {:as args}]
  {:pre [(validate-args-macro editor-args-desc args "editor")]}
  [editor-inner (update-in (merge default-args args) [:options] #(merge json-options %))])
