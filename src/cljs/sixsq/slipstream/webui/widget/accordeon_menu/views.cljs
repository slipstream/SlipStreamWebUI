(ns sixsq.slipstream.webui.widget.accordeon-menu.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-com.core :as rc]))

(rf/reg-event-db
  :accordeon/initialize
  (fn [_ _]
    {}))

(defn sub-key [header sub]
  (str header "-" sub))

(defn- display-dispatch-target-name [target]
  "Construct the name of the dispatch target. Use display since it sets the status of
   the menu display"
  (keyword (str target "/display")))

(defn gap []
  [:div {:style {:flex-grow 1000}}])

(defn subsections-menu [header subsections component-name s]
  (let [dispatch-target (display-dispatch-target-name component-name)]
    (doall (for [[sub j] (#(map list % (range)) subsections)]
             [rc/box
              :attr {:key (sub-key header sub)}
              :child [:a
                      {:data-dispatch (:data-dispatch sub)
                       :on-click      (fn []
                                        (swap! s assoc-in [:last-sub] (sub-key header j))
                                        (rf/dispatch [(:data-dispatch sub)])
                                        (rf/dispatch [dispatch-target :hide]))
                       :style         {:cursor "default"}}
                      [:div {:class (when (= (:last-sub @s) (sub-key header j))
                                      :accordeon-menu-sub-selected)}
                       (:content sub)]]]))))

(defn sections-menu [i-h-s component-name s]
  (doall
    (for [[i header subsections] i-h-s]
      [rc/v-box
       :attr {:key (str component-name "-" i)}
       :children [[rc/h-box
                   :class (str "accordeon-menu-header"
                            (when (= (:last-header @s) i)
                              " accordeon-menu-header-selected"))
                   :justify :between
                   :attr {:on-click (fn []
                                      (swap! s update-in [:open-sections]
                                        #(if (contains? % i) (disj % i) (conj % i)))
                                      (swap! s update-in [:last-header]
                                        #(if (or (empty? (:open-sections @s))
                                               (not (contains? (:open-sections @s) i)))
                                           nil
                                           (if (= % i) nil i))))}
                   :children [[:div header]
                              (if (contains? (:open-sections @s) i)
                                [:i.material-icons {:style {:cursor "default"}} "keyboard_arrow_down"]
                                [:i.material-icons {:style {:cursor "default"}} "keyboard_arrow_right"])]]
                  [rc/v-box
                   :class "accordeon-menu-sub"
                   :style {:height     (if (contains? (:open-sections @s) i)
                                         (when-let [el (get-in @s [:refs i])]
                                           (.-clientHeight el))
                                         0)
                           :overflow   :hidden
                           :transition "all 1s linear 0s"}
                   :attr {:key (sub-key header i)}
                   :children
                   (subsections-menu header subsections component-name s)]]])))

;;--------------------------------------------------------------------------------------------------
;; Component: accordeon-menu
;;--------------------------------------------------------------------------------------------------

(defn accordeon-menu [options & children]
  (let [{:keys [open-sections] :or {open-sections #{}}} options
        s (reagent/atom {:open-sections open-sections
                         :last-header   nil
                         :last-sub      nil})]
    (fn [options & children]
      (let [{:keys [component-name]} options
            pairs (partition-all 2 children)
            i-h-s (map conj pairs (range))]
        [rc/v-box
         :class "accordeon-menu"
         :attr {:id component-name}
         :children [
                    [rc/box
                     :class "accordeon-header"
                     :child [:div.webui-logo {:style {:width  "20ex"
                                                      :height "100%"}}]]
                    (sections-menu i-h-s component-name s)
                    [gap]
                    [rc/box
                     :class "webui-footer"
                     :child "Terms and conditions"]]]))))

; For doc...
;(defn main-panel []
;  (fn []
;    [:div#main
;     [accordeon-menu {:component-name "main-menu"
;                      :open-sections #{0 1}}
;      "Modules" [{:content [:p "Choice AA"] :data-dispatch :choice-aa}
;                     {:content [:p "Choice AB"] :data-dispatch :choice-ab}
;                     {:content [:p "Choice AC"] :data-dispatch :choice-ac}]
;      "Menu item 2" [{:content [:p "Choice BA"] :data-dispatch :choice-ba}
;                     {:content [:p "Choice BB"] :data-dispatch :choice-bb}
;                     {:content [:p "Choice BC"] :data-dispatch :choice-bc}]
;      "Menu item 3" [{:content [:p "Choice CA"] :data-dispatch :choice-ca}
;                     {:content [:p "Choice CB"] :data-dispatch :choice-cb}
;                     {:content [:p "Choice CC"] :data-dispatch :choice-cc}]]
;     [accordeon-menu-ctrl "main-menu"]]))
