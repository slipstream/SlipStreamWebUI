(ns sixsq.slipstream.webui.i18n.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [sixsq.slipstream.webui.i18n.dictionary :as dictionary]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]))

;; usage: (dispatch [:set-locale locale-id])
;; updates the current locale and the translation function
(reg-event-db
  :set-locale
  [db/check-spec-interceptor trim-v]
  (fn [db [locale-id]]
    (-> db
        (update-in [:i18n :locale] (constantly locale-id))
        (update-in [:i18n :tr] (constantly (dictionary/create-tr-fn locale-id))))))
