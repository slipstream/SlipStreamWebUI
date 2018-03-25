(ns sixsq.slipstream.webui.messages.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.messages.subs :as message-subs]
    [sixsq.slipstream.webui.messages.events :as message-events]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.time :as time]
    [reagent.core :as reagent]))


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


(defn message-modal
  []
  (let [alert-message (subscribe [::message-subs/alert-message])]
    (fn []
      (if-let [{:keys [type header content]} @alert-message]
        (let [icon-name (type->icon-name type)
              header-class (str "webui-" (name type))]
          [ui/Modal
           {:close-icon true
            :open       (boolean @alert-message)
            :on-close   #(dispatch [::message-events/hide])}
           [ui/ModalHeader {:class-name header-class}
            [ui/Icon {:size "big"
                      :name icon-name}]
            header]
           (when content
             [ui/ModalContent
              {:scrolling true}
              [:pre content]])])))))


(defn message-item-card
  [locale index {:keys [type header content timestamp]}]
  (let [visible? (reagent/atom false)]
    (fn [locale index {:keys [type header content timestamp]}]
      (let [header-class (str "webui-" (name type))]
        [ui/Card {:fluid true}
         [ui/Label {:as       :a
                    :corner   "right"
                    :size     "mini"
                    :icon     (if @visible? "chevron down" "chevron up")
                    :on-click #(reset! visible? (not @visible?))}]
         [ui/Label {:as       :a
                    :corner   "left"
                    :size     "mini"
                    :icon     "close"
                    :on-click #(dispatch [::message-events/remove index])}]
         [ui/CardContent {:class-name header-class}
          [ui/CardHeader
           [ui/Icon {:name (type->icon-name type)}]
           header]
          [ui/CardMeta (time/ago timestamp locale)]]
         (when @visible?
           [ui/CardContent {:class-name "webui-x-autoscroll"}
            [ui/CardDescription [:pre content]]])]))))


(defn message-list
  []
  (let [tr (subscribe [::i18n-subs/tr])
        locale (subscribe [::i18n-subs/locale])
        messages (subscribe [::message-subs/messages])]
    (if (seq @messages)
      (vec (concat [ui/ItemGroup]
                   (vec (map (fn [i msg] [message-item-card @locale i msg]) (range) @messages))))
      [ui/Header {:as "h1"} (@tr [:no-messages])])))


(defmethod panel/render :messages
  [path]
  [ui/Container
   [message-list]])

