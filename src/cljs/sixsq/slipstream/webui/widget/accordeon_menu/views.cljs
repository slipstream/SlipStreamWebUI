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

(defn menu-background [display]
  "Background of the menu when displayed in collapsed mode."
  (when (= :show display)
    [:div.accordeon-menu-background
     {:on-click #(rf/dispatch [:main-menu/display :hide])
      :style {:display          "block"
              :position         "fixed"
              :width            "100%"
              :height           "100%"
              :z-index          1}}]))

(defn- display-dispatch-target-name [target]
  "Construct the name of the dispatch target. Use display since it sets the status of
   the menu display"
  (keyword (str target "/display")))

(defn gap []
  [:div {:style {:flex-grow 1000}}])

;;--------------------------------------------------------------------------------------------------
;; Component: accordeon-menu
;;--------------------------------------------------------------------------------------------------

(defn accordeon-menu [options & children]
  (let [{:keys [open-sections] :or {open-sections #{}}} options
        s (reagent/atom {:open-sections open-sections
                         :last-header nil
                         :last-sub nil})]
    (fn [options & children]
      (let [{:keys [component-name]} options
            pairs (partition-all 2 children)
            i-h-s (map conj pairs (range))
            dispatch-target (display-dispatch-target-name component-name)
            display @(rf/subscribe [dispatch-target])]
          [rc/v-box
           :class "accordeon-menu"
           :attr {:id component-name}
           :children [
                       ;[menu-background display]
                       ;{:style {:position (if (= :show display) "fixed" "static")
                       ;                             :left     "0px"
                       ;                             :top      "0px"
                       ;                             :width    "200px"
                       ;                             :z-index  1020
                       ;                             :height "100vh"}
                       ;                     }
                       [rc/box
                        :class "accordeon-header"
                        :child [:div.webui-logo {:style {:width  "20ex"
                                                         :height "100%"}}]]
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
                              :style {:height (if (contains? (:open-sections @s) i)
                                                (when-let [el (get-in @s [:refs i])]
                                                  (.-clientHeight el))
                                                0)
                                      :overflow :hidden
                                      :transition "all 1s linear 0s"}
                              :attr {:key (sub-key header i)}
                              :children
                              (doall (for [[sub j] (#(map list % (range)) subsections)]
                                       [rc/box
                                        :attr {:key (sub-key header sub)}
                                        :child [:a
                                                {:data-dispatch (:data-dispatch sub)
                                                 :on-click (fn []
                                                             (swap! s assoc-in [:last-sub] (sub-key header j))
                                                             (rf/dispatch [(:data-dispatch sub)])
                                                             (rf/dispatch [dispatch-target :hide]))}
                                                [:div {:class (when (= (:last-sub @s) (sub-key header j))
                                                                :accordeon-menu-sub-selected)}
                                                 (:content sub)]]]))]]]))
                      [gap]
                      [rc/box
                       :class "webui-footer"
                       :child "Footer"]]]))))
                       ;(pr-str @s)


;(defn accordeon-menu-ctrl [component-name]
;  (let [dispatch-target (display-dispatch-target-name component-name)]
;    [:div.accordeon-menu-ctrl
;    [:a {:on-click #(let [display @(rf/subscribe [dispatch-target])]
;                      (rf/dispatch [dispatch-target (if (= :show display) :hide :show)]))}
;     [:i.material-icons "menu"]]]))

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
