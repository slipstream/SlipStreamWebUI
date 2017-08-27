(ns sixsq.slipstream.webui.components.editor
  (:require
    [re-com.util :refer [deref-or-value]]
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.javascript]
    [taoensso.timbre :as log]
    [clojure.string :as str]
    [cljs.spec.alpha :as s]))

(def default-options {:lineNumbers true
                      :mode        {:name "javascript", :json true}})

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

(defn json-editor-inner
  "Function that acts as the reagent component for the editor. This should
   only be used via the public component function."
  [& {:keys [options] :as args}]
  (let [cm (atom nil)
        text-area-id (str "json-editor-" (random-id))
        options (clj->js (merge default-options options))
        update (fn [comp old-argv]
                 (let [{:keys [text] :as args} (reagent/props comp)]
                   (.setValue @cm (or (deref-or-value text) ""))))]

    (reagent/create-class
      {:reagent-render       (fn [& {:as args}]
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

       :display-name         "json-editor"})))

;;
;; component options schema
;;

(s/def ::reagent-atom #(instance? reagent/atom %))

(s/def ::text (s/nilable (s/or :atom ::reagent-atom :string string?)))
(s/def ::on-change fn?)
(s/def ::options map?)
(s/def ::change-on-blur? boolean)

(s/fdef json-editor
        :args (s/keys* :req-un [::text ::on-change]
                       :opt-un [::options ::change-on-blur?]))

;;
;; public component
;;

(defn json-editor
  "Provides a JSON editor component based on Codemirror. The component takes
   keyword arguments. The supported keywords are:

   :text

      Initial text for the editor. Can be nil, a string, or a reagent atom
      containing a string.

   :options

      A map containing the options for the CodeMirror editor. See the
      CodeMirror documentation for details. The keys must be provided as
      keywords.  The given value is merged with the defaults.

   :on-change

      Function that is called on updates. The function will receive a single
      argument that contains the updated text.

   :change-on-blur?

      A boolean value that determines whether the on-change callback is called
      for every change (false) or just when the component blurs (true). The
      default is true."
  [& {:keys [text] :or {:change-on-blur? true} :as args}]
  [json-editor-inner args])
