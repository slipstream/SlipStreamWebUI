(ns sixsq.slipstream.webui.widget.operations.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.widget.i18n.dictionary :as dictionary]
    [re-frame.core :refer [reg-event-fx trim-v]]
    [taoensso.timbre :as log]))

(reg-event-fx
  :evt.webui.op/add
  [db/debug-interceptors trim-v]
  (fn [cofx [resource-type data]]
    (log/error "DEBUG EVENT" "ADD" resource-type data)
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.op/add [client resource-type data])
      cofx)))

(reg-event-fx
  :evt.webui.op/edit
  [db/debug-interceptors trim-v]
  (fn [cofx [href data]]
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.op/edit [client href data])
      cofx)))

(reg-event-fx
  :evt.webui.op/delete
  [db/debug-interceptors trim-v]
  (fn [cofx [href]]
    (log/error "DEBUG EVENT" "DELETE" href)
    (if-let [client (get-in cofx [:db :clients :cimi])]
      (assoc cofx :fx.webui.op/delete [client href])
      cofx)))
