(ns sixsq.slipstream.webui.widget.breadcrumbs.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.widget.breadcrumbs.utils :as u]))

(reg-event-fx
  :evt.webui.breadcrumbs/push-breadcrumb
  [db/debug-interceptors trim-v]
  (fn [cofx [crumb]]
    (let [path (-> cofx :db :navigation :path (conj crumb))
          relative-url (u/breadcrumbs->url path)]
      (assoc cofx :fx.webui.history/navigate [relative-url]))))
