(ns sixsq.slipstream.webui.application.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.application.effects :as application-fx]
    [sixsq.slipstream.webui.history.events :as history-evts]
    [sixsq.slipstream.webui.application.spec :as spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.main.spec :as main-spec]
    [sixsq.slipstream.webui.application.utils :as utils]
    [clojure.string :as str]))


(reg-event-db
  ::set-module
  (fn [db [_ module-id module]]
    (assoc db ::spec/completed? true
              ::spec/module-id module-id
              ::spec/module module)))


(reg-event-db
  ::open-add-modal
  (fn [db _]
    (assoc db ::spec/add-modal-visible? true)))


(reg-event-db
  ::close-add-modal
  (fn [db _]
    (assoc db ::spec/add-modal-visible? false)))


(defn fixup-image-data
  [{:keys [type connector image-id] :as data}]
  (if (= "IMAGE" type)
    (-> data
        (dissoc :connector :image-id)
        (assoc-in [:content :imageIDs] {(keyword connector) image-id}))
    data))


(reg-event-fx
  ::add-module
  (fn [{{:keys [::client-spec/client
                ::main-spec/nav-path
                ::spec/add-data
                ::spec/active-tab] :as db} :db} _]
    (when client
      (let [path (utils/nav-path->module-path nav-path)
            {project-name :name :as form-data} (get add-data active-tab)
            module-path (str path "/" project-name)
            data (-> form-data
                     (assoc :type (-> active-tab name str/upper-case)
                            :parentPath path
                            :path module-path)
                     fixup-image-data)]
        {::application-fx/create-module [client path data #(dispatch [::history-evts/navigate (str "application/" module-path)])]}))))


(reg-event-fx
  ::get-module
  (fn [{{:keys [::client-spec/client ::main-spec/nav-path] :as db} :db} _]
    (when client
      (let [path (utils/nav-path->module-path nav-path)]
        {:db                         (assoc db ::spec/completed? false
                                               ::spec/module-id nil
                                               ::spec/module nil)
         ::application-fx/get-module [client path #(dispatch [::set-module path %])]}))))


(reg-event-db
  ::update-add-data
  (fn [{:keys [::spec/add-data] :as db} [_ path value]]
    (assoc-in db (concat [::spec/add-data] path) value)))


(reg-event-db
  ::set-active-tab
  (fn [db [_ active-tab]]
    (assoc db ::spec/active-tab active-tab)))
