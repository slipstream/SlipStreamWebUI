;(ns vms.core
;  (:require-macros
;    [cljs.core.async.macros :refer [go]])
;  (:require [reagent.core :as reagent :refer [atom]]
;            [cljs.core.async :refer [<! >! chan timeout]]
;            [sixsq.slipstream.client.async :as async-client]
;            [sixsq.slipstream.client.impl.utils.http-async :as http]
;            [sixsq.slipstream.client.impl.utils.json :as json]
;            [sixsq.slipstream.client.api.cimi :as cimi]
;            [sixsq.slipstream.client.api.runs :as runs]
;            [soda-ash.core :as sa]
;            [vms.visibility :as vs]
;            [clojure.string :as str]))
;
;(enable-console-print!)
;
;(defonce webPageVisible (atom true))
;
;(defonce client (async-client/instance "https://localhost/api/cloud-entry-point" {:insecure? true}))
;
;(defonce activeTab (atom 0))
;
;(defonce activeVmPage (atom 1))
;
;(defonce vms-loading (atom true))
;
;(defonce activeDeploymentPage (atom 1))
;(defonce deployments-loading (atom true))
;(defonce toto (atom {}))
;
;(defn extract-deployments-data [deployment-resp]
;  (js/console.log deployment-resp)
;  (let [deployments (get-in deployment-resp [:runs :item] [])]
;    (map (fn [{:keys [uuid moduleResourceUri status startTime cloudServiceNames username]}]
;           {:deployment-href (str "run/" uuid)
;            :module          moduleResourceUri
;            :state           status
;            :active-vm       0
;            :start-time      startTime
;            :clouds          cloudServiceNames
;            :user-href       username}) deployments)))
;
;(defn fetch-deployments []
;  (go
;    (reset! toto (<! (runs/search-runs client {:offset     "0"
;                                                               :limit      "10"
;                                                               :cloud      ""
;                                                               :activeOnly 0}))))
;  (reset! deployments-loading false))
;
;(def deployments-table-headers
;  ["ID" "Application / Component" "Service URL" "State" "Active VMs" "Start Time" "Clouds" "User"]) ;TODO remove user ? ask Cal
;
;(vs/VisibleWebPage {:onWebPageVisible #(reset! webPageVisible true)
;                    :onWebPageHidden  #(reset! webPageVisible false)})
;
;(defonce vms-response (atom {}))
;
;(defonce number-record-display 10)
;
;(defn fetch-vms []
;  (go
;    (reset! vms-response (<! (cimi/search client "virtualMachines" {"$first"   (-
;                                                                                 (* @activeVmPage number-record-display)
;                                                                                 (dec number-record-display))
;                                                                    "$last"    (* @activeVmPage number-record-display)
;                                                                    "$orderby" "created:desc"
;                                                                    ;"$filter"  "deployment/href!=null"
;                                                                    }))))
;  (reset! vms-loading false))                               ; TODO remove filter
;
;(go (while true
;      (when @webPageVisible
;        (case @activeTab
;          0 (fetch-deployments)
;          1 (fetch-vms)))
;      (<! (timeout 10000))))
;
;(def vms-table-headers
;  ["ID" "State" "IP" "CPU" "RAM [MB]" "DISK [GB]" "Instance type" "Cloud Instance ID" "Cloud" "Owner"])
;
;(defn table-vm-row [{:keys [deployment-href state ip vcpu ram disk instance-type instance-id connector-href user-href]}]
;  [[sa/TableCell {:collapsing true}
;    (when (empty? deployment-href)
;      [sa/Popup {:trigger  (reagent/as-element [:div [sa/Icon {:name "exclamation circle"}] "Unkown"])
;                 :inverted true
;                 :size     "mini" :content "Deployment UUID unknown" :position "left center"}])
;    [:a {:href deployment-href} (or (-> deployment-href
;                                        (str/replace #"^run/" "")
;                                        (str/split #"-")
;                                        (first)) "")]]
;   [sa/TableCell {:collapsing true} state]
;   [sa/TableCell {:collapsing true} ip]
;   [sa/TableCell {:collapsing true :textAlign "center"} vcpu]
;   [sa/TableCell {:collapsing true :textAlign "center"} ram]
;   [sa/TableCell {:collapsing true :textAlign "center"} disk]
;   [sa/TableCell {:collapsing true} instance-type]
;   [sa/TableCell {:collapsing true} instance-id]
;   [sa/TableCell {:collapsing true} (or (-> connector-href (str/replace #"^connector/" "")) "")]
;   (let [owner-full-text (str/replace user-href #"^user/" "")
;         owner-text (if (> (count owner-full-text) 20) (-> owner-full-text (subs 0 20) (str "...")) owner-full-text)]
;     [sa/TableCell [:a {:href user-href} owner-text]])])
;
;(defn extract-vms-data [vms-response]
;  (let [vms (:virtualMachines vms-response)]
;    (map (fn [{:keys [ip state instanceID deployment serviceOffer connector]}]
;           {:deployment-href (get deployment :href "")
;            :state           (or state "")
;            :ip              (or ip "")
;            :vcpu            (get serviceOffer :resource:vcpu "")
;            :ram             (get serviceOffer :resource:ram "")
;            :disk            (get serviceOffer :resource:disk "")
;            :instance-type   (get serviceOffer :resource:instanceType "")
;            :instance-id     (or instanceID "")
;            :connector-href  (get connector :href "")
;            :user-href       (get-in deployment [:user :href] "")}) vms)))
;
;(defn vms-table []
;  (let [first-button (atom 1)
;        number-button-nav 5]
;    (fn []
;      [sa/Table
;       {:compact     "very"
;        :size        "small"
;        :selectable  true
;        :celled      true
;        :single-line true
;        :padded      false}
;
;       [sa/TableHeader
;        (vec (concat [sa/TableRow]
;                     (vec (map (fn [label] ^{:key label} [sa/TableHeaderCell label]) vms-table-headers))))]
;       (vec (concat [sa/TableBody]
;                    (vec (map
;                           (fn [entry]
;                             (vec (concat [sa/TableRow
;                                           {:error (empty? (:deployment-href entry))}] (table-vm-row entry))))
;                           (extract-vms-data @vms-response)))))
;
;       (let [vms-count (get @vms-response :count 0)
;             page-count (quot vms-count number-record-display)
;             button-range (take number-button-nav (range @first-button (inc page-count)))]
;         [sa/TableFooter
;          [sa/TableRow
;           [sa/TableHeaderCell {:col-span "10"}
;            [sa/Label "VMs found" [sa/LabelDetail vms-count]]
;            [sa/Menu {:floated "right" :size "mini"}
;             [sa/MenuItem {:link true :onClick (fn [e d]
;                                                 (reset! activeVmPage 1)
;                                                 (reset! first-button 1)
;                                                 (reset! vms-loading true)
;                                                 (fetch-vms))}
;              [sa/Icon {:name "angle double left"}]]
;             [sa/MenuItem {:link true :onClick (fn [e d] (when (> @activeVmPage 1)
;                                                           (swap! activeVmPage dec)
;                                                           (when (< @activeVmPage (first button-range))
;                                                             (swap! first-button dec))
;                                                           (reset! vms-loading true)
;                                                           (fetch-vms)
;                                                           ))}
;              [sa/Icon {:name "angle left"}]]
;             (doall
;               (for [i button-range]
;                 ^{:key i} [sa/MenuItem {:link    true
;                                         :active  (= @activeVmPage i)
;                                         :onClick (fn [e d]
;                                                    (reset! activeVmPage (.-children d))
;                                                    (reset! vms-loading true)
;                                                    (fetch-vms)
;                                                    )} i]
;                 )
;               )
;             [sa/MenuItem {:link true :onClick (fn [e d] (when (< @activeVmPage page-count)
;                                                           (swap! activeVmPage inc)
;                                                           (when (> @activeVmPage (last button-range))
;                                                             (swap! first-button inc))
;                                                           (reset! vms-loading true)
;                                                           (fetch-vms)
;                                                           ))}
;              [sa/Icon {:name "angle right"}]]
;             [sa/MenuItem {:link true :onClick (fn [e d]
;                                                 (reset! activeVmPage page-count)
;                                                 (reset! vms-loading true)
;                                                 (reset! first-button
;                                                         (let [first-should-be (- page-count (dec number-button-nav))]
;                                                           (if (neg-int? first-should-be)
;                                                             1 first-should-be)))
;                                                 (fetch-vms))}
;              [sa/Icon {:name "angle double right"}]]
;             ]]]
;          ])
;       ]                                                    ;]
;      )))
;
;(defn deployments-table []
;  [:p (str @toto)]
;  )
;
;(defn app []
;  (let [tab1-loading @deployments-loading
;        tab2-loading @vms-loading]
;    [sa/Tab
;     {:onTabChange (fn [e, d] (reset! activeTab (.-activeIndex d)))
;      :panes       [{:menuItem "Deployments"
;                     :render   (fn [] (reagent/as-element [sa/TabPane {:loading tab1-loading} [deployments-table]]))}
;                    {:menuItem "Virtual Machines"
;                     :render   (fn [] (reagent/as-element [sa/TabPane {:loading tab2-loading} [vms-table]]))}
;                    ]}]))
;
;(reagent/render-component [app]
;                          (. js/document (getElementById "app")))
