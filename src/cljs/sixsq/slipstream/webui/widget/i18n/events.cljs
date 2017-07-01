(ns sixsq.slipstream.webui.widget.i18n.events
  (:require
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.widget.i18n.dictionary :as dictionary]
    [re-frame.core :refer [reg-event-db trim-v]]))

(reg-event-db
  :evt.webui.i18n/set-locale
  [db/debug-interceptors trim-v]
  (fn [db [locale-id]]
    (-> db
        (update-in [:i18n :locale] (constantly locale-id))
        (update-in [:i18n :tr] (constantly (dictionary/create-tr-fn locale-id))))))
