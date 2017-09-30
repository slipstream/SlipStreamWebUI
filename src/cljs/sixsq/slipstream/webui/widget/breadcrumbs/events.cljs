(ns sixsq.slipstream.webui.widget.breadcrumbs.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.webui.db :as db]
    [sixsq.slipstream.webui.widget.breadcrumbs.utils :as u]))

(defn- assoc-navigate-fx [cofx path]
  (let [relative-url (u/breadcrumbs->url path)]
    (assoc cofx :fx.webui.history/navigate [relative-url])))

(reg-event-fx
  :push-breadcrumb
  [db/debug-interceptors trim-v]
  (fn [{{:keys [resource-path]} :db :as cofx} [crumb]]
    (assoc-navigate-fx cofx (conj resource-path crumb))))
