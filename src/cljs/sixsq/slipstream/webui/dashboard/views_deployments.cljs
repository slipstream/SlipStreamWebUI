(ns sixsq.slipstream.webui.dashboard.views-deployments
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [cljs.core.async :refer [<! >! chan timeout]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [taoensso.timbre :as log]
    [clojure.string :as str]
    #_[sixsq.slipstream.legacy.utils.tables :as t]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.dashboard.subs :as dashbord-subs]
    [sixsq.slipstream.webui.dashboard.events :as dashboard-events]))

(def app-state (r/atom {:web-page-visible true
                        :deployments      {}
                        :request-opts     {:offset     0
                                           :limit      10
                                           :cloud      ""
                                           :activeOnly 1}
                        :loading          true
                        :headers          ["" "ID" "Application / Component" "Service URL" "State" "VMs"
                                           "Start Time [UTC]" "Clouds" "Tags" "User" ""]
                        :message          {:hidden  true
                                           :error   true
                                           :header  ""
                                           :content ""}
                        :show-modal       nil
                        :deleted          #{}}))

(defn state-set-web-page-visible [v]
  (swap! app-state assoc :web-page-visible v))

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

(defn state-set-show-modal [v]
  (swap! app-state assoc :show-modal v))

(defn state-append-deleted-deployment [v]
  (swap! app-state update-in [:deleted] conj v))

(defn state-pop-deleted-deployment [v]
  (swap! app-state update-in [:deleted] disj v))

(defn current-page [] (inc (/ (request-opts-offset) (request-opts-limit))))

(defn extract-deployments-data [deployment-resp]
  (let [deployments (get-in deployment-resp [:runs :item] [])]
    (map (fn [{:keys [uuid moduleResourceUri serviceUrl status startTime cloudServiceNames username abort type
                      tags activeVm]}]
           {:deployment-uuid uuid
            :module-uri      moduleResourceUri
            :service-url     serviceUrl
            :state           status
            :start-time      startTime
            :clouds          cloudServiceNames
            :user            username
            :tags            tags
            :abort           abort
            :type            type
            :activeVm        activeVm
            }) deployments)))

(defn is-terminated-state? [state]
  (#{"Finalizing" "Done" "Aborted" "Cancelled"} state))

#_(defn terminate-confirm [{:keys [deployment-uuid module-uri state clouds start-time abort tags service-url]
                            :as   deployment}]
    [:div [ui/Icon (if (contains? (get @app-state :deleted) deployment-uuid)
                     {:name "trash outline" :color "black"}
                     {:name "remove" :color "red" :link true :onClick #(state-set-show-modal deployment-uuid)})]
     [ui/Confirm {:open      (= (get @app-state :show-modal) deployment-uuid)
                  :basic     true
                  :content   (r/as-element
                               [ui/Table {:unstackable true
                                          :celled      false
                                          :single-line true
                                          :inverted    true
                                          :size        "small"
                                          :padded      false}
                                [ui/TableBody
                                 [ui/TableRow
                                  [ui/TableCell "ID:"]
                                  [ui/TableCell [:a {:href (str "run/" deployment-uuid)} deployment-uuid]]]
                                 [ui/TableRow
                                  [ui/TableCell "Module URI:"]
                                  [ui/TableCell [:a {:href module-uri} module-uri]]]
                                 [ui/TableRow [ui/TableCell "Start time:"] [ui/TableCell start-time]]
                                 [ui/TableRow [ui/TableCell "State:"] [ui/TableCell state]]
                                 [ui/TableRow
                                  [ui/TableCell "Service URL:"]
                                  [ui/TableCell [:a {:href service-url :target "_blank"} service-url]]]
                                 [ui/TableRow [ui/TableCell "State:"] [ui/TableCell state]]
                                 [ui/TableRow [ui/TableCell "Clouds:"] [ui/TableCell clouds]]
                                 [ui/TableRow [ui/TableCell "Abort:"] [ui/TableCell abort]]
                                 [ui/TableRow [ui/TableCell "Tags:"] [ui/TableCell tags]]]
                                ])
                  :onCancel  #(state-set-show-modal nil)
                  :onConfirm #(do
                                (state-append-deleted-deployment deployment-uuid)
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
                                (go (<! (timeout 30000))
                                    (state-pop-deleted-deployment deployment-uuid))
                                (fetch-deployments)
                                (state-set-show-modal nil))
                  }]]
    )


(defn table-deployment-cells
  [{:keys [deployment-uuid module-uri service-url state start-time clouds user state tags abort type activeVm]
    :as   deployment}]
  (let [global-prop (if (is-terminated-state? state) {:disabled true} {})]
    [[ui/TableCell (merge {:collapsing true} global-prop)
      [ui/Icon (cond
                 (and (= state "Ready") (empty? abort)) {:name "checkmark"}
                 (not-empty abort) {:name "exclamation circle"}
                 (is-terminated-state? state) {:name "power"}
                 :else {:loading true :name "spinner"}
                 )]
      [ui/Icon {:name (case type
                        "Orchestration" "grid layout"
                        "Run" "laptop"
                        "Machine" "industry"
                        ""
                        )}]]
     [ui/TableCell {:collapsing true}
      [:a {:href (str "run/" deployment-uuid)} (-> deployment-uuid
                                                   (str/split #"-")
                                                   (first))]]
     [ui/TableCell {:collapsing true :style {:max-width "150px" :overflow "hidden" :text-overflow "ellipsis"}}
      (let [module-uri-vec (-> module-uri (str/split #"/"))
            module-uri-version (str (nth module-uri-vec (- (count module-uri-vec) 2)) " " (last module-uri-vec))]
        [:a {:href module-uri} module-uri-version])]
     [ui/TableCell {:collapsing true :style {:max-width "200px" :overflow "hidden" :text-overflow "ellipsis"}}
      [:a {:href service-url :target "_blank"}
       (when-not (empty? service-url) [ui/Icon {:name "external"}]) service-url]]
     [ui/TableCell (merge {:collapsing true} global-prop) state]
     [ui/TableCell (merge {:collapsing true :textAlign "center"} global-prop) activeVm]
     [ui/TableCell (merge {:collapsing true} global-prop) (first (str/split start-time #"\."))]
     [ui/TableCell (merge {:collapsing true} global-prop) clouds]
     [ui/TableCell (merge {:collapsing true :style
                                       {:max-width "100px" :overflow "hidden" :text-overflow "ellipsis"}}
                          global-prop) tags]
     [ui/TableCell {:style {:max-width "100px" :overflow "hidden" :text-overflow "ellipsis"}}
      [:a {:href (str "user/" user)} user]]
     (if (is-terminated-state? state)
       [ui/TableCell {:collapsing true}]
       [ui/TableCell {:collapsing true} #_(terminate-confirm deployment)])
     ]))

(defn table-row-format [{:keys [state abort] :as deployment}]
  (let [aborted (not-empty abort)
        opts (cond
               (and (= state "Ready") (not aborted)) {:positive true}
               aborted {:error true}
               :else {})
        row (vec (concat [ui/TableRow opts] (table-deployment-cells deployment)))]
    (if aborted
      [ui/Popup {:trigger  (r/as-element row)
                 :inverted true
                 :size     "mini" :header "ss:abort" :content abort :position "top center"}]
      row)))

(defn deployments-table [cloud-filter]
  #_(log/debug @app-state)
  (let [deployments (subscribe [::dashbord-subs/deployments])
        headers ["" "ID" "Application / Component" "Service URL" "State" "VMs"
                 "Start Time [UTC]" "Clouds" "Tags" "User" ""]
        record-displayed (subscribe [::dashbord-subs/records-displayed])
        page (subscribe [::dashbord-subs/page])
        total-pages (subscribe [::dashbord-subs/total-pages])
        active-only (subscribe [::dashbord-subs/active-deployments-only])]
    (fn []
      (let [deployments-count (get-in @deployments [:runs :totalCount] 0)]
        [ui/Segment {:basic true
                     ;:loading (get @app-state :loading)
                     }
         #_[ui/Message (cond-> {:header    (r/as-element [:div (get-in @app-state [:message :header])])
                                :content   (r/as-element [:div (get-in @app-state [:message :content])])
                                :hidden    (get-in @app-state [:message :hidden])
                                :onDismiss #(state-set-message :hidden true)}
                               (get-in @app-state [:message :error]) (merge {:icon "exclamation circle" :error true}))]
         [ui/Checkbox {:slider   true :fitted true :label "Include inactive runs"
                       :onChange #(dispatch [::dashboard-events/active-deployments-only
                                             (not (:checked (js->clj %2 :keywordize-keys true)))])}]
         [ui/Table
          {:compact     "very"
           :size        "small"
           :selectable  true
           :unstackable true
           :celled      false
           :single-line true
           :collapsing  false
           :padded      false}

          [ui/TableHeader
           (vec (concat [ui/TableRow]
                        (vec (map (fn [i label] ^{:key (str i "_" label)}
                        [ui/TableHeaderCell label]) (range) headers))))]
          (vec (concat [ui/TableBody]
                       (vec (map table-row-format
                                 (extract-deployments-data @deployments)))))
          [ui/TableFooter
           [ui/TableRow
            [ui/TableHeaderCell {:col-span (str 3)}
             [ui/Label "Found" [ui/LabelDetail deployments-count]]]
            [ui/TableHeaderCell {:textAlign "right"
                                 :col-span  (str (- (count headers) 3))}
             [ui/Pagination
              {:size         "tiny"
               :totalPages   @total-pages
               :activePage   @page
               :onPageChange (fn [e d]
                               (dispatch [::dashboard-events/set-page (:activePage (js->clj d :keywordize-keys true))]))
               }]]]]
          #_[t/table-navigator-footer
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
          ]]
        )
      )
    )
  )