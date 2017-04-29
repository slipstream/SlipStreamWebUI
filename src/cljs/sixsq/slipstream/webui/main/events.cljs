(ns sixsq.slipstream.webui.main.events
  (:require
    [sixsq.slipstream.webui.main.db :as db]
    [sixsq.slipstream.webui.main.effects :as effects]
    [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
    [sixsq.slipstream.client.api.cimi.async :as cimi-async]
    [sixsq.slipstream.client.api.runs.async :as runs-async]
    [sixsq.slipstream.client.api.modules.async :as modules-async]
    [sixsq.slipstream.webui.utils :as utils]
    [sixsq.slipstream.webui.history :as history]
    [clojure.set :as set]
    [clojure.string :as str]))

;; usage:  (dispatch [:initialize-db])
;; creates initial state of database
(reg-event-fx
  :initialize-db
  [db/check-spec-interceptor]
  (fn [_ _]
    {:db db/default-value}))

;; usage:  (dispatch [:initialize-history])
;; triggers initial entry in application history
(reg-event-fx
  :initialize-history
  [db/check-spec-interceptor]
  (fn [{:keys [db]} _]
    {:db db
     :history/initialize []}))

;; usage:  (dispatch [:initialize-client])
;; creates and adds a SlipStream client to the database
(reg-event-db
  :initialize-client
  [db/check-spec-interceptor trim-v]
  (fn [db [slipstream-url]]
    (let [clients {:cimi    (cimi-async/instance (str slipstream-url "/api/cloud-entry-point")
                                                 (str slipstream-url "/auth/login")
                                                 (str slipstream-url "/auth/logout"))
                   :runs    (runs-async/instance (str slipstream-url "/run")
                                                 (str slipstream-url "/auth/login")
                                                 (str slipstream-url "/auth/logout"))
                   :modules (modules-async/instance (str slipstream-url "/module")
                                                    (str slipstream-url "/auth/login")
                                                    (str slipstream-url "/auth/logout"))}]
      (assoc db :client (:cimi clients)
                :clients clients))))

;; usage: (dispatch [:message msg])
;; displays a message
(reg-event-fx
  :message
  [db/check-spec-interceptor trim-v]
  (fn [{:keys [db]} [msg]]
    {:db             (assoc db :message msg)
     :dispatch-later [{:ms 3000 :dispatch [:clear-message]}]}))

;; usage: (dispatch [:clear-message])
;; clears a message
(reg-event-db
  :clear-message
  [db/check-spec-interceptor]
  (fn [db _]
    (assoc db :message nil)))

(defn parse-resource-path
  "Utility to split a resource path into a vector of terms.
   Returns an empty vector for a nil argument.  Removes
   blank or empty terms from the result."
  [path]
  (vec (remove str/blank? (str/split path #"/"))))

;; usage:  (dispatch [:set-resource-path path])
(reg-event-db
  :set-resource-path
  [db/check-spec-interceptor trim-v]
  (fn [db [path]]
    (assoc db :resource-path (parse-resource-path path))))

