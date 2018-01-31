(ns sixsq.slipstream.webui.client.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.client.spec :as client-spec]))


(reg-sub
  ::client
  (fn [db]
    (::client-spec/client db)))

(reg-sub
  ::slipstream-url
  (fn [db]
    (::client-spec/slipstream-url db)))
