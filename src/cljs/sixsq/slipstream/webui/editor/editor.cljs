(ns sixsq.slipstream.webui.editor.editor
  (:require
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.javascript]
    [taoensso.timbre :as log]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.component :as c-utils]
    [cljs.spec.alpha :as s]))


(def json-options {:lineNumbers true
                   :mode        {:name "javascript", :json true}})

(def default-args {:options         {}
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
  [text {:keys [options] :as args}]
  (let [ref (reagent/atom nil)
        editor-id (str "editor-" (random-id))
        options (clj->js options)]
    (reagent/create-class
      {:display-name "codemirror-editor"
       :component-did-mount
                     (fn [comp]
                       (let [cm-instance (.fromTextArea js/CodeMirror (.getElementById js/document editor-id) options)]
                         (.on cm-instance "blur" (callback-with-text #(reset! text %)))
                         (.on cm-instance "change" (callback-with-text #(reset! text %)))))
       :reagent-render
                     (fn [text {:keys [options] :as args}]
                       [ui/Form
                        [ui/TextArea {:id    editor-id
                                      :value @text}]])})))

  (defn editor
    "Provides an editor component based on Codemirror. To use modes other than
     javascript, you will have to require the mode explicitly in your code."
    [text & {:as args}]
    [editor-inner text (merge default-args args)])

  (defn json-editor
    "A convenience function to setup the CodeMirror editor component for JSON."
    [text & {:as args}]
    [editor-inner text (update-in (merge default-args args) [:options] #(merge json-options %))])
