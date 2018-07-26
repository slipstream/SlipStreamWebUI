(ns sixsq.slipstream.webui.messages.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.messages.events :as message-events]
    [sixsq.slipstream.webui.messages.subs :as message-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn type->icon-name
  [type]
  (case type
    :error "warning circle"
    :info "info circle"
    :success "check circle"
    "warning circle"))


(defn type->message-type
  [type]
  (case type
    :error {:error true}
    :info {:info true}
    :success {:success true}
    {:info true}))


(defn message-detail-modal
  [icon-name header content visible? f]
  [ui/Modal
   {:close-icon true
    :open       @visible?
    :on-close   #(do
                   (reset! visible? false)
                   (when f (f)))}
   [ui/ModalHeader
    [ui/Icon {:name icon-name}]
    header]
   (when content
     [ui/ModalContent {:scrolling true}
      [:pre content]])])


(defn alert-slider
  []
  (let [tr (subscribe [::i18n-subs/tr])
        alert-message (subscribe [::message-subs/alert-message])
        alert-display (subscribe [::message-subs/alert-display])]
    (fn []
      (if-let [{:keys [type header]} @alert-message]
        (let [icon-name (type->icon-name type)
              open? (boolean (and @alert-message (= :slider @alert-display)))
              transition (clj->js {:animation "slide left"
                                   :duration  500})
              top-right {:position "fixed", :top "0", :right "0", :zIndex 1000}]
          [ui/TransitionablePortal {:transition transition, :open open?}
           [ui/Message (merge (type->message-type type)
                              {:size       "mini"
                               :style      top-right
                               :on-dismiss #(dispatch [::message-events/hide])})
            [ui/MessageHeader [ui/Icon {:name icon-name}] header "\u2001\u00a0"]
            [:a {:on-click #(dispatch [::message-events/open-modal])} (@tr [:more-info])]]])))))


(defn alert-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        alert-message (subscribe [::message-subs/alert-message])
        alert-display (subscribe [::message-subs/alert-display])]
    (if-let [{:keys [type header content]} @alert-message]
      (let [icon-name (type->icon-name type)
            visible? (= :modal @alert-display)
            hide-fn #(dispatch [::message-events/hide])
            remove-fn #(dispatch [::message-events/remove @alert-message])]
        [ui/Modal {:open       visible?
                   :close-icon true
                   :on-close   hide-fn}

         [ui/ModalHeader [ui/Icon {:name icon-name}] header "\u2001\u00a0"]

         [ui/ModalContent {:scrolling true}
          (when content [:pre content])]

         [ui/ModalActions
          [ui/Button {:on-click hide-fn} (@tr [:close])]
          [ui/Button {:negative :true, :on-click remove-fn} (@tr [:clear])]]]))))


(defn feed-item
  [locale {:keys [type header timestamp] :as message}]
  (let [icon-name (type->icon-name type)]
    [ui/ListItem
     [ui/ListIcon {:name           icon-name
                   :size           "large"
                   :vertical-align "middle"}]
     [ui/ListContent
      [ui/ListHeader {:as "a", :on-click #(dispatch [::message-events/show message])}
       header]
      [ui/ListDescription (time/ago timestamp locale)]]]))


(defn message-feed
  []
  (let [locale (subscribe [::i18n-subs/locale])
        messages (subscribe [::message-subs/messages])]
    (when (seq @messages)
      (vec (concat [ui/ListSA {:divided false
                               :relaxed true
                               :style   {:height       "100%"
                                         :max-height   "40ex"
                                         :overflow-y   "auto"
                                         :margin-right "1ex"}}]
                   (mapv (partial feed-item @locale) @messages))))))


(defn bell-menu
  "Provides a messages menu icon that will bring up the list of recent
   messages. If there are no messages, the item will be disabled. If there are
   messages, then a label will show the number of them."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        session (subscribe [::authn-subs/session])
        messages (subscribe [::message-subs/messages])
        popup-open? (subscribe [::message-subs/popup-open?])]
    (when @session
      (let [n (count @messages)]
        [ui/MenuItem {:disabled (zero? n)
                      :fitted   "horizontally"}
         [ui/Popup {:on       "click"
                    :position "bottom right"
                    :open     @popup-open?
                    :on-open  #(dispatch [::message-events/open-popup])
                    :on-close #(dispatch [::message-events/close-popup])
                    :trigger  (reagent/as-element [ui/Label {:as "a"}
                                                   [ui/Icon {:name "bell outline"}]
                                                   (str n)])}
          [ui/PopupHeader (@tr [:notifications])]
          [ui/PopupContent [ui/Divider]]
          [ui/PopupContent [message-feed]]
          [ui/PopupContent
           [ui/Divider]
           [ui/Button {:fluid    true
                       :negative true
                       :compact  true
                       :on-click #(dispatch [::message-events/clear-all])}
            (@tr [:clear])]]]]))))
