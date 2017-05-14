(ns sixsq.slipstream.webui.widget.i18n.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [sixsq.slipstream.webui.widget.i18n.dictionary :as dictionary]
    [re-frame.core :refer [reg-event-db trim-v]]))

(reg-event-db
  :evt.webui.i18n/set-locale
  [db/check-spec-interceptor trim-v]
  (fn [db [locale-id]]
    (-> db
        (update-in [:i18n :locale] (constantly locale-id))
        (update-in [:i18n :tr] (constantly (dictionary/create-tr-fn locale-id))))))
