(ns sixsq.slipstream.webui.main.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.main.spec :as main-spec]
    [sixsq.slipstream.webui.main.effects :as main-fx]
    [sixsq.slipstream.webui.history.effects :as history-fx]
    [taoensso.timbre :as log]
    [clojure.string :as str]))


(reg-event-db
  ::toggle-sidebar
  (fn [{:keys [::main-spec/sidebar-open?] :as db} _]
    (update db ::main-spec/sidebar-open? not sidebar-open?)))

(reg-event-fx
  ::visible
  (fn [{:keys [db]} [_ v]]
    {:db                       (assoc db ::main-spec/visible? v)
     ::main-fx/action-interval (if v [{:action :resume}] [{:action :pause}])}))


(reg-event-fx
  ::set-navigation-info
  (fn [{:keys [db]} [_ path query-params]]
    (let [path-vec (vec (str/split path #"/"))]
      (log/info "navigation path:" path)
      (log/info "navigation query params:" query-params)
      {:db                       (merge db {::main-spec/nav-path         path-vec
                                            ::main-spec/nav-query-params query-params})
       ::main-fx/action-interval [{:action :clean}]})))

(reg-event-fx
  ::action-interval
  (fn [_ [_ opts]]
    {::main-fx/action-interval [opts]}))

(reg-event-fx
  ::push-breadcrumb
  (fn [{{:keys [::main-spec/nav-path] :as db} :db} [_ path-element]]
    {::history-fx/navigate [(str/join "/" (conj nav-path path-element))]}))

(reg-event-fx
  ::trim-breadcrumb
  (fn [{{:keys [::main-spec/nav-path] :as db} :db} [_ index]]
    {::history-fx/navigate [(str/join "/" (take (inc index) nav-path))]}))
