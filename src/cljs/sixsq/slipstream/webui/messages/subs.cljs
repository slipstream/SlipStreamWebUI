(ns sixsq.slipstream.webui.messages.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.messages.spec :as messages-spec]))


(reg-sub
  ::messages
  (fn [db]
    (::messages-spec/messages db)))


(reg-sub
  ::alert-message
  (fn [db]
    (::messages-spec/alert-message db)))
