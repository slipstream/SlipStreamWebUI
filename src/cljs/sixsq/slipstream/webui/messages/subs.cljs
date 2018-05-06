(ns sixsq.slipstream.webui.messages.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.messages.spec :as messages-spec]))


(reg-sub
  ::messages
  ::messages-spec/messages)


(reg-sub
  ::alert-message
  ::messages-spec/alert-message)
