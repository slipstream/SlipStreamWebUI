(ns sixsq.slipstream.dashboard-tabs.deployments
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [soda-ash.core :as sa]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [promesa.core :as p]
            [promesa.async-cljs :refer-macros [async]]
            [sixsq.slipstream.client.api.runs :as runs]
            [sixsq.slipstream.client.api.cimi :as cimi]
            [sixsq.slipstream.dashboard-tabs.utils.client :as client]
            [sixsq.slipstream.dashboard-tabs.utils.tables :as t]))

(def app-state (atom {:deployments  {}
                      :request-opts {:offset     0
                                     :limit      10
                                     :cloud      ""
                                     :activeOnly 1}
                      :loading      true
                      :headers      ["" "ID" "Application / Component" "Service URL" "State" "VMs" "Start Time [UTC]"
                                     "Clouds" "Tags" "User" ""]
                      :message      {:hidden  true
                                     :error   true
                                     :header  ""
                                     :content ""}}))

(defn state-set-loading [v]
      (swap! app-state assoc :loading v))

(defn state-disable-loading []
      (state-set-loading false))

(defn state-enable-loading []
      (state-set-loading true))

(defn state-set-request-opts [k v]
      (swap! app-state assoc-in [:request-opts k] v))

(defn state-set-offset [v]
      (state-set-request-opts :offset v))

(defn state-set-limit [v]
      (state-set-request-opts :limit v))

(defn state-set-cloud [v]
      (state-set-request-opts :cloud v))

(defn state-set-active-only [v]
      (state-set-request-opts :activeOnly v))

(defn state-set-deployments [v]
      (swap! app-state assoc :deployments v))

(defn request-opts-limit []
      (get-in @app-state [:request-opts :limit]))

(defn request-opts-offset []
      (get-in @app-state [:request-opts :offset]))

(defn state-set-message [k v]
      (swap! app-state assoc-in [:message k] v))

(defn set-page [page]
      (state-set-offset (* (dec page) (request-opts-limit))))

(defn deployments-count [] (get-in @app-state [:deployments :runs :totalCount] 0))

(defn page-count []
      (let [dc (deployments-count)
            limit (request-opts-limit)
            full-page-number (quot dc limit)
            additionnal-page (if (pos? (mod dc limit)) 1 0)]
           (+ full-page-number additionnal-page)))

(defn current-page [] (inc (/ (request-opts-offset) (request-opts-limit))))

(defn inc-page []
      (let [cp (current-page)]
           (when (< cp (page-count))
                 (set-page (inc cp)))))

(defn dec-page []
      (let [cp (current-page)]
           (when (> cp 1)
                 (set-page (dec cp)))))

(defn fetch-active-vms [resolve deployments-list]
      (go
        (let [deployments-uuid (map #(str "deployment/href=\"run/" (:uuid %) "\"") deployments-list)
              filter-str (str/join " or " deployments-uuid)
              response (<! (cimi/search client/client "virtualMachines" {"$last"        0
                                                                         "$aggregation" "terms:deployment/href"
                                                                         "$filter"      filter-str}))
              active-vms-per-deployment (->> (get-in response [:aggregations :terms:deployment/href :buckets] [])
                                             (map #(vector (keyword (:key %)) (:doc_count %)))
                                             (into {}))]
             (resolve active-vms-per-deployment))))

(defn extract-deployments-data [deployment-resp]
      (let [deployments (get-in deployment-resp [:runs :item] [])]
           (map (fn [{:keys [uuid moduleResourceUri serviceUrl status startTime cloudServiceNames username abort type
                             tags active-vm]}]
                    {:deployment-uuid uuid
                     :module-uri      moduleResourceUri
                     :service-url     serviceUrl
                     :state           status
                     :start-time      startTime
                     :clouds          cloudServiceNames
                     :user-href       username
                     :tags            tags
                     :abort           abort
                     :type            type
                     :active-vm       active-vm
                     }) deployments)))

(defn fetch-deployments []
      (go
        (let [response (<! (runs/search-runs client/client (get @app-state :request-opts)))
              item (get-in response [:runs :item] [])
              deployments-list (if (= (type item) cljs.core/PersistentVector) item [item])]
             ; workaround gson issue, when one element in item it give element instead of a vector of element

             (if (empty? deployments-list)
               (do (state-set-deployments (assoc-in response [:runs :item] deployments-list))
                   (state-disable-loading))
               (-> (p/promise
                     (fn [resolve _] (fetch-active-vms resolve deployments-list)))
                   (p/then (fn [active-vms-per-deployment]
                               (->> deployments-list
                                    (map #(assoc % :active-vm
                                                 (get active-vms-per-deployment (keyword (str "run/" (:uuid %))) 0)))
                                    (assoc-in response [:runs :item])
                                    state-set-deployments)
                               (state-disable-loading))))
               ))))

(defn is-terminated-state? [state]
      (#{"Finalizing" "Done" "Aborted" "Cancelled"} state))

(defn terminate-confirm [{:keys [deployment-uuid module-uri state clouds start-time abort tags service-url]
                          :as   deployment}]
      (let [show-modal (atom false)
            deleting (atom false)]
           (fn []
               [:div [sa/Icon (if @deleting
                                {:name "trash outline" :color "black"}
                                {:name "remove" :color "red" :link true :onClick #(reset! show-modal true)})]
                [sa/Confirm {:open      @show-modal
                             :basic     true
                             :content   (reagent/as-element
                                          [:div
                                           [:h3 (str "Are you sure to terminate following deployment?")]
                                           [sa/Table {:unstackable true
                                                      :celled      false
                                                      :single-line true
                                                      :inverted    true
                                                      :size        "small"
                                                      :padded      false}
                                            [sa/TableBody
                                             [sa/TableRow
                                              [sa/TableCell "ID:"]
                                              [sa/TableCell [:a {:href (str "run/" deployment-uuid)} deployment-uuid]]]
                                             [sa/TableRow
                                              [sa/TableCell "Module URI:"]
                                              [sa/TableCell [:a {:href module-uri} module-uri]]]
                                             [sa/TableRow [sa/TableCell "Start time:"] [sa/TableCell start-time]]
                                             [sa/TableRow [sa/TableCell "State:"] [sa/TableCell state]]
                                             [sa/TableRow
                                              [sa/TableCell "Service URL:"]
                                              [sa/TableCell [:a {:href service-url :target "_blank"} service-url]]]
                                             [sa/TableRow [sa/TableCell "State:"] [sa/TableCell state]]
                                             [sa/TableRow [sa/TableCell "Clouds:"] [sa/TableCell clouds]]
                                             [sa/TableRow [sa/TableCell "Abort:"] [sa/TableCell abort]]
                                             [sa/TableRow [sa/TableCell "Tags:"] [sa/TableCell tags]]]
                                            ]])
                             :onCancel  #(reset! show-modal false)
                             :onConfirm #(do
                                           (reset! deleting true)
                                           (go
                                             (let [result (<! (runs/terminate-run client/client deployment-uuid))
                                                   error (when (instance? js/Error result)
                                                               (:error (js->clj
                                                                         (->> result ex-data :body (.parse js/JSON))
                                                                         :keywordize-keys true)))]
                                                  (when error
                                                        (state-set-message :header (str (get error :reason "-")
                                                                                        ": "
                                                                                        (get error :code "-")))
                                                        (state-set-message :content (get error :detail "-"))
                                                        (state-set-message :error true)
                                                        (state-set-message :hidden false))
                                                  ))
                                           (fetch-deployments)
                                           (reset! show-modal false))
                             }]]
               )))


(defn table-deployment-cells
      [{:keys [deployment-uuid module-uri service-url state start-time clouds user-href state tags abort type active-vm]
        :as   deployment}]
      (let [global-prop (if (is-terminated-state? state) {:disabled true} {})]
           [[sa/TableCell (merge {:collapsing true} global-prop)
             [sa/Icon (cond
                        (and (= state "Ready") (empty? abort)) {:name "checkmark"}
                        (not-empty abort) {:name "exclamation circle"}
                        (is-terminated-state? state) {:name "power"}
                        :else {:loading true :name "spinner"}
                        )]
             [sa/Icon {:name (case type
                                   "Orchestration" "grid layout"
                                   "Run" "laptop"
                                   "Machine" "industry"
                                   ""
                                   )}]]
            [sa/TableCell {:collapsing true}
             [:a {:href (str "run/" deployment-uuid)} (-> deployment-uuid
                                                          (str/split #"-")
                                                          (first))]]
            [sa/TableCell {:collapsing true :style {:max-width "150px" :overflow "hidden" :text-overflow "ellipsis"}}
             (let [module-uri-vec (-> module-uri (str/split #"/"))
                   module-uri-version (str (nth module-uri-vec (- (count module-uri-vec) 2)) " " (last module-uri-vec))]
                  [:a {:href module-uri} module-uri-version])]
            [sa/TableCell {:collapsing true :style {:max-width "200px" :overflow "hidden" :text-overflow "ellipsis"}}
             [:a {:href service-url :target "_blank"}
              (when-not (empty? service-url) [sa/Icon {:name "external"}]) service-url]]
            [sa/TableCell (merge {:collapsing true} global-prop) state]
            [sa/TableCell (merge {:collapsing true :textAlign "center"} global-prop) active-vm]
            [sa/TableCell (merge {:collapsing true} global-prop) (first (str/split start-time #"\."))]
            [sa/TableCell (merge {:collapsing true} global-prop) clouds]
            [sa/TableCell (merge {:collapsing true :style
                                              {:max-width "100px" :overflow "hidden" :text-overflow "ellipsis"}}
                                 global-prop) tags]
            [sa/TableCell {:style {:max-width "100px" :overflow "hidden" :text-overflow "ellipsis"}}
             [:a {:href user-href} user-href]]
            (if (is-terminated-state? state)
              [sa/TableCell {:collapsing true}]
              [sa/TableCell {:collapsing true} [terminate-confirm deployment]])
            ]))

(defn table-row-format [{:keys [state abort] :as deployment}]
      (let [aborted (not-empty abort)
            opts (cond
                   (and (= state "Ready") (not aborted)) {:positive true}
                   aborted {:error true}
                   :else {})
            row (vec (concat [sa/TableRow opts] (table-deployment-cells deployment)))]
           (if aborted
             [sa/Popup {:trigger  (reagent/as-element row)
                        :inverted true
                        :size     "mini" :header "ss:abort" :content abort :position "top center"}]
             row)))

(defn deployments-table [cloud-filter]
      (log/info (-> @app-state
                    (dissoc :deployments)
                    (dissoc :headers)))
      [sa/Segment {:basic true :loading (get @app-state :loading)}
       [sa/Message (cond-> {:header    (reagent/as-element [:div (get-in @app-state [:message :header])])
                            :content   (reagent/as-element [:div (get-in @app-state [:message :content])])
                            :hidden    (get-in @app-state [:message :hidden])
                            :onDismiss #(state-set-message :hidden true)}
                           (get-in @app-state [:message :error]) (merge {:icon "exclamation circle" :error true}))]
       [sa/Checkbox {:slider   true :fitted true :label "Include inactive runs"
                     :onChange #(do
                                  (state-set-active-only (if (:checked (js->clj %2 :keywordize-keys true)) 0 1))
                                  (set-page 1)
                                  (fetch-deployments))}]
       [sa/Table
        {:compact     "very"
         :size        "small"
         :selectable  true
         :unstackable true
         :celled      false
         :single-line true
         :collapsing  false
         :padded      false}

        [sa/TableHeader
         (vec (concat [sa/TableRow]
                      (vec (map (fn [i label] ^{:key (str i "_" label)}
                      [sa/TableHeaderCell label]) (range) (get @app-state :headers)))))]
        (vec (concat [sa/TableBody]
                     (vec (map table-row-format
                               (extract-deployments-data (get @app-state :deployments))))))
        [t/table-navigator-footer
         deployments-count
         page-count
         current-page
         #(count (get @app-state :headers))
         #(do (inc-page)
              (state-enable-loading)
              (fetch-deployments))
         #(do
            (dec-page)
            (state-enable-loading)
            (fetch-deployments))
         #(do (set-page 1)
              (state-enable-loading)
              (fetch-deployments))
         #(do (set-page (page-count))
              (state-enable-loading)
              (fetch-deployments))
         #(do (set-page (.-children %2))
              (state-enable-loading)
              (fetch-deployments))]
        ]])

(defn ^:export setCloudFilter [cloud]
      (if (= cloud "All Clouds")
        (state-set-cloud "")
        (state-set-cloud cloud))
      (state-enable-loading)
      (fetch-deployments))
