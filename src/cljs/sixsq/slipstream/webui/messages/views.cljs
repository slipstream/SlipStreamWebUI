(ns sixsq.slipstream.webui.messages.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.messages.events :as message-events]
    [sixsq.slipstream.webui.messages.subs :as message-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn bell-menu
  "Provides a messages menu icon that will bring up the list of recent
   messages. If there are no messages, the item will be disabled. If there are
   messages, then a label will show the number of them."
  []
  (let [session (subscribe [::authn-subs/session])
        messages (subscribe [::message-subs/messages])]
    (fn []
      (when @session
        (let [n (count @messages)]
          [ui/MenuItem {:disabled (zero? n)
                        :fitted   "horizontally"
                        :on-click #(dispatch [::history-events/navigate "messages"])}
           [ui/Label {:size "large"}
            [ui/Icon {:name "bell"}]
            (str n)]])))))


(defn type->icon-name
  [type]
  (case type
    :error "warning circle"
    :info "info circle"
    :success "check circle"
    "warning circle"))


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
           [ui/Message {:size       "mini"
                        :success    true
                        :style      top-right
                        :on-dismiss #(dispatch [::message-events/hide])}
            [ui/MessageHeader [ui/Icon {:name icon-name}] header "\u2001\u00a0"]
            [:a {:on-click #(dispatch [::message-events/open-modal])} (@tr [:more-info])]]])))))


(defn alert-modal
  []
  (let [alert-message (subscribe [::message-subs/alert-message])
        alert-display (subscribe [::message-subs/alert-display])]
    (fn []
      (if-let [{:keys [type header content]} @alert-message]
        (let [icon-name (type->icon-name type)
              visible? (= :modal @alert-display)]
          [ui/Modal
           {:close-icon true
            :open       visible?
            :on-close   #(dispatch [::message-events/hide])}
           [ui/ModalHeader [ui/Icon {:name icon-name}] header "\u2001\u00a0"]
           (when content
             [ui/ModalContent {:scrolling true}
              [:pre content]])])))))


(defn message-item
  [locale index {:keys [type header content timestamp]}]
  (let [visible? (reagent/atom false)]
    (fn [locale index {:keys [type header content timestamp]}]
      (let [icon-name (type->icon-name type)]
        [ui/ListItem {:on-click #(reset! visible? (not @visible?))}
         [ui/ListIcon {:name icon-name, :size "large"}]
         [ui/ListContent
          [ui/Button {:floated  "right"
                      :size     "tiny"
                      :icon     "close"
                      :on-click (fn [e]
                                  (when e
                                    (.stopPropagation e true))
                                  (dispatch [::message-events/remove index]))}]
          [ui/ListHeader header]
          [ui/ListDescription (time/ago timestamp locale)]]

         [ui/Modal
          {:close-icon true
           :open       @visible?
           :on-close   #(reset! visible? false)}
          [ui/ModalHeader
           [ui/Icon {:name icon-name}]
           header]
          (when content
            [ui/ModalContent {:scrolling true}
             [:pre content]])]]))))


(defn message-list-as-list
  []
  (let [tr (subscribe [::i18n-subs/tr])
        locale (subscribe [::i18n-subs/locale])
        messages (subscribe [::message-subs/messages])]
    (if (seq @messages)
      (vec (concat [ui/ListSA {:selection     true
                               :verticalAlign "middle"}]
                   (mapv (fn [i msg] [message-item @locale i msg]) (range) @messages)))
      [ui/Header {:as "h1"} (@tr [:no-messages])])))


(defmethod panel/render :messages
  [path]
  [ui/Container {:text true}
   [message-list-as-list]])

