(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.main.events
  (:require
    [re-frame.core :refer [reg-event-db debug]]
    [cubic.main.spec :as main-spec]
    [taoensso.timbre :as log]
    [clojure.string :as str]))


(reg-event-db
  ::toggle-sidebar
  [debug]
  (fn [{:keys [::main-spec/sidebar-open?] :as db} _]
    (update db ::main-spec/sidebar-open? not)))


(reg-event-db
  ::set-navigation-info
  (fn [db [_ path query-params]]
    (let [path-vec (vec (str/split path #"/"))]
      (log/info "navigation path:" path)
      (log/info "navigation query params:" query-params)
      (merge db {::main-spec/nav-path         path-vec
                 ::main-spec/nav-query-params query-params}))))


(reg-event-db
  ::push-breadcrumb
  (fn [db [_ path-element]]
    (update db ::main-spec/nav-path conj path-element)))


(reg-event-db
  ::trim-breadcrumb
  (fn [db [_ index]]
    (update db ::main-spec/nav-path (partial take (inc index)))))


