(ns vms.deployments
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [sixsq.slipstream.client.api.runs :as runs]
            [soda-ash.core :as sa]
            [vms.visibility :as vs]
            [vms.client-utils :as req]
            [clojure.string :as str]))

(defonce activeDeploymentPage (atom 1))
(defonce deployments-response (atom {}))
(defonce number-record-display 10)
(defonce with-inactive-deployments (atom false))
(defonce loading (atom true))

(defn extract-deployments-data [deployment-resp]
  (let [deployments (get-in deployment-resp [:runs :item] [])]
    (map (fn [{:keys [uuid moduleResourceUri serviceUrl status startTime cloudServiceNames username abort type]}]
           {:deployment-href (str "run/" uuid)
            :module-uri      moduleResourceUri
            :service-url     serviceUrl
            :state           status
            :active-vm       0
            :start-time      startTime
            :clouds          cloudServiceNames
            :user-href       username
            :abort           abort
            :type            type
            }) deployments)))

(defn fetch-deployments []
  (go
    (reset! deployments-response (<! (runs/search-runs req/client
                                                       {:offset     (-
                                                                      (* @activeDeploymentPage number-record-display)
                                                                      (dec number-record-display)
                                                                      1)
                                                        :limit      number-record-display
                                                        :cloud      ""
                                                        :activeOnly (if @with-inactive-deployments 0 1)}))))
  (reset! loading false))

(defn is-final-state? [state]
  (#{"Done" "Cancelled"} state))

(def deployments-table-headers
  ["" "ID" "Application / Component" "Service URL" "State" "VMs" "Start Time" "Clouds" "User" ""])

(defn table-deployment-cells
  [{:keys [deployment-href module-uri service-url state active-vm start-time clouds user-href state abort type]}]
  (let [global-prop (if (is-final-state? state) {:disabled true} {})]
    [[sa/TableCell (merge {:collapsing true} global-prop)
      [sa/Icon (cond
                 (and (= state "Ready") (empty? abort)) {:name "checkmark"}
                 (not-empty abort) {:name "exclamation circle"}
                 (is-final-state? state) {:name "power"}
                 :else {:loading true :name "spinner"}
                 )]
      [sa/Icon {:name (case type
                        "Orchestration" "grid layout"
                        "Run" "laptop"
                        "Machine" "industry"
                        )}]]
     [sa/TableCell {:collapsing true}
      [:a {:href deployment-href} (-> deployment-href
                                      (str/replace #"^run/" "")
                                      (str/split #"-")
                                      (first))]]
     [sa/TableCell {:collapsing true :style {:max-width "200px" :overflow "hidden" :text-overflow "ellipsis"}}
      (let [module-uri-vec (-> module-uri (str/split #"/"))
            module-uri-version (str (nth module-uri-vec (- (count module-uri-vec) 2)) " " (last module-uri-vec))]
        [:a {:href module-uri} module-uri-version])]
     [sa/TableCell {:collapsing true :style {:max-width "250px" :overflow "hidden" :text-overflow "ellipsis"}}
      [:a {:href service-url :target "_blank"}
       (when-not (empty? service-url) [sa/Icon {:name "external"}]) service-url]]
     [sa/TableCell (merge {:collapsing true} global-prop) state]
     [sa/TableCell (merge {:collapsing true :textAlign "center"} global-prop) active-vm]
     [sa/TableCell (merge {:collapsing true} global-prop) start-time]
     [sa/TableCell (merge {:collapsing true} global-prop) clouds]
     [sa/TableCell {:style {:max-width "150px" :overflow "hidden" :text-overflow "ellipsis"}}
      [:a {:href user-href} user-href]]
     (if (is-final-state? state)
       [sa/TableCell {:collapsing true}]
       [sa/TableCell {:collapsing true}
        [sa/Modal {:basic   true
                   ;:onOpen  #(reset! refresh false)
                   ;:onClose #(reset! refresh true)
                   :trigger (reagent/as-element [sa/Icon {:name "remove" :color "red" :link true}])}
         [sa/ModalHeader "Terminate deployment"]
         [sa/ModalContent [:p "Are you sure you want to terminate all virtual machines running in this deployment?"]
          [:p "Note that this cannot be undone."]]
         [sa/ModalActions
          [sa/Button {:basic true :color "red" :inverted true :onClick #(print "click")} [sa/Icon {:name "remove"}] "No"]
          [sa/Button {:basic true :color "green" :inverted true} [sa/Icon {:name "checkmark"}] "Yes"]]
         ]]
       )

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


(defn deployments-table []
  (let [first-button (atom 1)
        number-button-nav 5]
    (fn []
      [sa/Segment {:basic true :loading @loading}
       [sa/Checkbox {:slider   true :fitted true :label "Include inactive runs"
                     :onChange #(do (reset! with-inactive-deployments (:checked (js->clj %2 :keywordize-keys true)))
                                    (fetch-deployments)
                                    )}]
       [sa/Table
        {:compact     "very"
         :size        "small"
         :selectable  true
         :unstackable true
         :celled      false
         :single-line true
         :padded      false}

        [sa/TableHeader
         (vec (concat [sa/TableRow]
                      (vec (map (fn [i label] ^{:key (str i "_" label)}
                      [sa/TableHeaderCell label]) (range) deployments-table-headers))))]
        (vec (concat [sa/TableBody]
                     (vec (map
                            table-row-format
                            (extract-deployments-data @deployments-response)))))

        (let [deployments-count (get-in @deployments-response [:runs :totalCount] 0)
              page-count (quot deployments-count number-record-display)
              button-range (take number-button-nav (range @first-button (inc page-count)))]
          [sa/TableFooter
           [sa/TableRow
            [sa/TableHeaderCell {:col-span (str (count deployments-table-headers))}
             [sa/Label "Deployments found" [sa/LabelDetail deployments-count]]
             [sa/Menu {:floated "right" :size "mini"}
              [sa/MenuItem {:link true :onClick (fn [e d]
                                                  (reset! activeDeploymentPage 1)
                                                  (reset! first-button 1)
                                                  (reset! loading true)
                                                  (fetch-deployments))}
               [sa/Icon {:name "angle double left"}]]
              [sa/MenuItem {:link true :onClick (fn [e d] (when (> @activeDeploymentPage 1)
                                                            (swap! activeDeploymentPage dec)
                                                            (when (< @activeDeploymentPage (first button-range))
                                                              (swap! first-button dec))
                                                            (reset! loading true)
                                                            (fetch-deployments)
                                                            ))}
               [sa/Icon {:name "angle left"}]]
              (doall
                (for [i button-range]
                  ^{:key i} [sa/MenuItem {:link    true
                                          :active  (= @activeDeploymentPage i)
                                          :onClick (fn [e d]
                                                     (reset! activeDeploymentPage (.-children d))
                                                     (reset! loading true)
                                                     (fetch-deployments)
                                                     )} i]
                  )
                )
              [sa/MenuItem {:link true :onClick (fn [e d] (when (< @activeDeploymentPage page-count)
                                                            (swap! activeDeploymentPage inc)
                                                            (when (> @activeDeploymentPage (last button-range))
                                                              (swap! first-button inc))
                                                            (reset! loading true)
                                                            (fetch-deployments)
                                                            ))}
               [sa/Icon {:name "angle right"}]]
              [sa/MenuItem {:link true :onClick (fn [e d]
                                                  (reset! activeDeploymentPage page-count)
                                                  (reset! loading true)
                                                  (reset! first-button
                                                          (let [first-should-be (- page-count (dec number-button-nav))]
                                                            (if (neg-int? first-should-be)
                                                              1 first-should-be)))
                                                  (fetch-deployments))}
               [sa/Icon {:name "angle double right"}]]
              ]]]
           ])
        ]]
      )))

