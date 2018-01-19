(ns sixsq.slipstream.webui.editor.editor
  (:require
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.javascript]
    [taoensso.timbre :as log]
    [clojure.string :as str]
    [cljs.spec.alpha :as s]))

;; from re-com
(defn deref-or-value
  "Takes a value or an atom
  If it's a value, returns it
  If it's a Reagent object that supports IDeref, returns the value inside it by derefing
  "
  [val-or-atom]
  (if (satisfies? IDeref val-or-atom)
    @val-or-atom
    val-or-atom))


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
  [{:name        :text
    :required    true
    :type        "nil | string | atom"
    :description "string or atom that holds the text to be edited"}

   {:name        :options
    :required    false
    :type        "options map"
    :validate-fn map?
    :description "map of CodeMirror options"}

   {:name        :on-change
    :required    true
    :type        "update fn"
    :validate-fn fn?
    :description "1-arg (updated text) function called on updates"}

   {:name        :change-on-blur?
    :required    false
    :type        "boolean"
    :validate-fn boolean?
    :description "call update function only on blur (true, default) or on all changes (false)"}])

(defn editor
  "Provides an editor component based on Codemirror. To use modes other than
   javascript, you will have to require the mode explicitly in your code."
  [& {:as args}]
  [editor-inner (merge default-args args)])

(defn json-editor
  "A convenience function to setup the CodeMirror editor component for JSON."
  [& {:as args}]
  [editor-inner (update-in (merge default-args args) [:options] #(merge json-options %))])
