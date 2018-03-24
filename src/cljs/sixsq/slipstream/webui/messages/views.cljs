(ns sixsq.slipstream.webui.messages.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.messages.subs :as message-subs]
    [sixsq.slipstream.webui.messages.events :as message-events]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.time :as time]))


(defn bell-menu
  "Provides a messages menu icon that will bring up the list of recent
   messages. If there are no messages, the item will be disabled. If there are
   messages, then a label will show the number of them."
  []
  (let [messages (subscribe [::message-subs/messages])]
    (fn []
      (let [n (count @messages)]
        [ui/MenuItem {:disabled (zero? n)
                      :fitted   "horizontally"
                      :on-click #(dispatch [::history-events/navigate "messages"])}
         [ui/Icon {:name "bell"}]
         (when (pos? n)
           [ui/Label {:size "mini"}
            (str n)])]))))


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


(defn message-item
  [index {:keys [type header content timestamp]}]
  [ui/Item
   [ui/ItemContent
    [ui/ItemHeader
     [ui/Icon {:name (type->icon-name type)}]
     header]
    [ui/ItemMeta (time/remaining timestamp)]
    [ui/ItemDescription content]
    [ui/ItemExtra [ui/Button
                   {:on-click #(dispatch [::message-events/remove index])}
                   "clear"]]]])


(defn message-list
  []
  (let [messages (subscribe [::message-subs/messages])]
    (fn []
      (when @messages
        (vec (concat [ui/ItemGroup]
                     (vec (map message-item (range) @messages))))))))


(defmethod panel/render :messages
  [path]
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container {:textAlign "center"}
     [ui/Header {:as "h1"}
      (@tr [:messages])]
     [message-list]]))

