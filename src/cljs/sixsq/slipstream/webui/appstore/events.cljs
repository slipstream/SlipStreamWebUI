(ns sixsq.slipstream.webui.appstore.events
  (:require
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
    [sixsq.slipstream.webui.appstore.effects :as appstore-fx]
    [sixsq.slipstream.webui.appstore.spec :as spec]
    [sixsq.slipstream.webui.appstore.spec :as spec]
    [sixsq.slipstream.webui.client.spec :as client-spec]
    [sixsq.slipstream.webui.history.events :as history-evts]
    [clojure.string :as str]
    [taoensso.timbre :as log]))


(reg-event-db
  ::set-modules
  (fn [db [_ modules]]
    (assoc db ::spec/modules modules)))

(defn get-query-params
  [parent-path-search full-text-search page elements-per-page]
  (let [filter-str (str/join " and "
                             (cond-> ["type!='PROJECT'"
                                      (str "path^='" parent-path-search
                                           (when-not (str/blank? parent-path-search) "/") "'")]
                                     (not-empty full-text-search) (conj (str "description=='"
                                                                             full-text-search "*'"))))]
    {:$first   (inc (* (dec page) elements-per-page))
     :$last    (* page elements-per-page)
     :$orderby "name"
     :$select  "id, name, description, type, parentPath, path, logo"
     :$filter  filter-str}))


(reg-event-fx
  ::set-full-text-search
  (fn [{{:keys [::client-spec/client
                ::spec/parent-path-search
                ::spec/elements-per-page] :as db} :db} [_ full-text-search]]
    (let [new-page 1]
      {:db                       (assoc db ::spec/full-text-search full-text-search
                                           ::spec/page new-page)
       ::appstore-fx/get-modules [client
                                  (get-query-params parent-path-search full-text-search new-page elements-per-page)
                                  #(dispatch [::set-modules %])]})))


(reg-event-fx
  ::get-modules
  (fn [{{:keys [::client-spec/client
                ::spec/parent-path-search
                ::spec/full-text-search
                ::spec/page
                ::spec/elements-per-page] :as db} :db} _]
    (when client
      {:db                       (assoc db ::spec/modules nil)
       ::appstore-fx/get-modules [client
                                  (get-query-params parent-path-search full-text-search page elements-per-page)
                                  #(dispatch [::set-modules %])]})))


(reg-event-fx
  ::set-parent-path-search
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/elements-per-page] :as db} :db} [_ new-parent-path]]
    (let [new-page 1]
      {:db                       (assoc db ::spec/parent-path-search new-parent-path
                                           ::spec/paths []
                                           ::spec/page new-page)
       ::appstore-fx/get-paths   [client new-parent-path #(dispatch [::set-paths %])]
       ::appstore-fx/get-modules [client
                                  (get-query-params new-parent-path full-text-search new-page elements-per-page)
                                  #(dispatch [::set-modules %])]})))


(reg-event-db
  ::set-paths
  (fn [db [_ paths]]
    (->> paths
         (map (fn [{:keys [id name] :as module}] {:key id :text name :value name}))
         (assoc db ::spec/paths))))


(reg-event-fx
  ::get-paths
  (fn [{{:keys [::client-spec/client
                ::spec/parent-path-search] :as db} :db} _]
    (log/error "::get-paths" parent-path-search)
    {:db                     (assoc db ::spec/paths [])
     ::appstore-fx/get-paths [client parent-path-search #(dispatch [::set-paths %])]}))

(reg-event-fx
  ::set-page
  (fn [{{:keys [::client-spec/client
                ::spec/full-text-search
                ::spec/parent-path-search
                ::spec/page
                ::spec/elements-per-page] :as db} :db} [_ page]]
    {:db                       (assoc db ::spec/page page)
     ::appstore-fx/get-modules [client
                                (get-query-params parent-path-search full-text-search page elements-per-page)
                                #(dispatch [::set-modules %])]}))

(reg-event-fx
  ::get-deployment-templates
  (fn [{{:keys [::client-spec/client
                ::spec/deploy-module] :as db} :db} _]
    (when client
      {:db                                    (assoc db ::spec/deployment-templates nil
                                                        ::spec/loading-deployment-templates? true
                                                        ::spec/deploy-modal-visible? true)
       ::appstore-fx/get-deployment-templates [client (:id deploy-module) #(dispatch [::set-deployment-templates %])]})))


(reg-event-fx
  ::open-deploy-modal
  (fn [{{:keys [::client-spec/client] :as db} :db} [_ module]]
    (when client
      {:db                                    (assoc db ::spec/deployment-templates nil
                                                        ::spec/loading-deployment-templates? true
                                                        ::spec/deploy-modal-visible? true
                                                        ::spec/deploy-module module)
       ::appstore-fx/get-deployment-templates [client (:id module) #(dispatch [::set-deployment-templates %])]})))


(reg-event-db
  ::close-deploy-modal
  (fn [db _]
    (assoc db ::spec/deploy-modal-visible? false)))



(reg-event-db
  ::set-selected-deployment-template
  (fn [{:keys [::spec/deployment-templates] :as db} [_ template]]
    (assoc db ::spec/selected-deployment-template template)))


(reg-event-db
  ::set-deployment-templates
  (fn [db [_ deployment-templates]]
    (assoc db ::spec/deployment-templates deployment-templates
              ::spec/loading-deployment-templates? false)))


(reg-event-fx
  ::create-deployment-template
  (fn [{{:keys [::client-spec/client
                ::spec/deploy-module]} :db :as cofx} _]
    (when client
      (assoc cofx ::appstore-fx/create-deployment-template
                  [client (:id deploy-module) #(dispatch [::get-deployment-templates])]))))


(reg-event-fx
    ::deploy
    (fn [{{:keys [::client-spec/client ::spec/selected-deployment-template]} :db :as cofx} _]
      (when (and client (:id selected-deployment-template))
        (assoc cofx ::appstore-fx/deploy
                    [client selected-deployment-template #(dispatch [::history-evts/navigate (str "cimi/" %)])]))))
