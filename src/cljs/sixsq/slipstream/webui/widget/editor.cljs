(ns sixsq.slipstream.webui.widget.editor
  (:require
    [re-frame.core :refer [subscribe]]
    [reagent.core :as reagent]
    [cljsjs.codemirror]
    [cljsjs.codemirror.mode.clojure]
    [cljsjs.codemirror.mode.javascript]
    [taoensso.timbre :as log]))

(defn cm-inner []
  (let [cm (atom nil)
        options (clj->js {:lineNumbers true
                          :mode        {:name "javascript", :json true}})
        update (fn [comp]
                 (let [{:keys [data]} (reagent/props comp)
                       text (.stringify js/JSON (clj->js data) nil 2)]
                   (.setValue @cm text)))]

    (reagent/create-class
      {:reagent-render       (fn []
                               [:textarea#cm-textarea {:style {:height "400px"}}])

       :component-did-mount  (fn [comp]
                               (let [textarea (.getElementById js/document "cm-textarea")
                                     cm-instance (.fromTextArea js/CodeMirror textarea options)]
                                 (reset! cm cm-instance))
                               (update comp))

       :component-did-update update
       :display-name         "cm-inner"})))

(defn cm-outer []
  (let [_ (subscribe [:cloud-entry-point])]
    (fn [data]
      [cm-inner data])))
