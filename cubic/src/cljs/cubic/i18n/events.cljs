(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.i18n.events
  (:require
    [re-frame.core :refer [reg-event-db]]
    [cubic.i18n.utils :as utils]))


(reg-event-db
  ::set-locale
  (fn [{:keys [:cubic.main.spec/sidebar-open?] :as db} [_ locale]]
    (-> db
        (assoc :cubic.i18n.spec/locale locale)
        (assoc :cubic.i18n.spec/tr (utils/create-tr-fn locale)))))
