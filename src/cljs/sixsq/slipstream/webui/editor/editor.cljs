(ns sixsq.slipstream.webui.editor.editor
  (:require
    [cljs.spec.alpha :as s]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.javascript]
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.general :as general]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(def json-options {:lineNumbers true
                   :mode        {:name "javascript", :json true}})

(def default-args {:options         {}
                   :change-on-blur? true})


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
        editor-id (str "editor-" (general/random-element-id))
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
