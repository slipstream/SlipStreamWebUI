(ns vms.core
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [sixsq.slipstream.client.api.cimi :as cimi]
            [soda-ash.core :as sa]
            [vms.visibility :as vs]
            [vms.client-utils :as req]
            [vms.deployments :as dep]
            [clojure.string :as str]))

; TODO filter via cloud gauge
; Export component utils
; Done not refresh if windows not visible
; Done not refresh when other tab open
; Paging
; manage table content and size and enable scroll
; test integration with existing dashboard (left 2 issue, modal not centered and height body)
; color
; create helper components

(enable-console-print!)

(defonce webPageVisible (atom true))
(defonce activeTab (atom 0))
(defonce number-record-display 10)
(defonce activeVmPage (atom 1))

(defonce vms-loading (atom true))

(vs/VisibleWebPage {:onWebPageVisible #(reset! webPageVisible true)
                    :onWebPageHidden  #(reset! webPageVisible false)})

(defonce vms-response (atom {}))

(defn fetch-vms []
  (go
    (reset! vms-response (<! (cimi/search req/client "virtualMachines" {"$first"   (-
                                                                                     (* @activeVmPage number-record-display)
                                                                                     (dec number-record-display))
                                                                        "$last"    (* @activeVmPage number-record-display)
                                                                        "$orderby" "created:desc"
                                                                        ;"$filter"  "deployment/href!=null"
                                                                        }))))
  (reset! vms-loading false))                               ; TODO remove filter

(defonce refresh (atom true))

(go (while true
      (js/console.log "refreshing!!!")
      (when (and @webPageVisible @refresh)
        (case @activeTab
          0 (dep/fetch-deployments)
          1 (fetch-vms)))
      (<! (timeout 10000))))

(def vms-table-headers
  ["ID" "State" "IP" "CPU" "RAM [MB]" "DISK [GB]" "Instance type" "Cloud Instance ID" "Cloud" "Owner"])

(defn table-vm-cells [{:keys [deployment-href state ip vcpu ram disk instance-type instance-id connector-href user-href]}]
  [[sa/TableCell {:collapsing true}
    (when (empty? deployment-href)
      [sa/Popup {:trigger  (reagent/as-element [:div [sa/Icon {:name "exclamation circle"}] "Unkown"])
                 :inverted true
                 :size     "mini" :content "Deployment UUID unknown" :position "left center"}])
    [:a {:href deployment-href} (or (-> deployment-href
                                        (str/replace #"^run/" "")
                                        (str/split #"-")
                                        (first)) "")]]
   [sa/TableCell {:collapsing true} state]
   [sa/TableCell {:collapsing true} ip]
   [sa/TableCell {:collapsing true :textAlign "center"} vcpu]
   [sa/TableCell {:collapsing true :textAlign "center"} ram]
   [sa/TableCell {:collapsing true :textAlign "center"} disk]
   [sa/TableCell {:collapsing true} instance-type]
   [sa/TableCell {:collapsing true} instance-id]
   [sa/TableCell {:collapsing true} (or (-> connector-href (str/replace #"^connector/" "")) "")]
   (let [owner-full-text (str/replace user-href #"^user/" "")
         owner-text (if (> (count owner-full-text) 20) (-> owner-full-text (subs 0 20) (str "...")) owner-full-text)]
     [sa/TableCell [:a {:href user-href} owner-text]])])


(defn extract-vms-data [vms-response]
  (let [vms (:virtualMachines vms-response)]
    (map (fn [{:keys [ip state instanceID deployment serviceOffer connector]}]
           {:deployment-href (get deployment :href "")
            :state           (or state "")
            :ip              (or ip "")
            :vcpu            (get serviceOffer :resource:vcpu "")
            :ram             (get serviceOffer :resource:ram "")
            :disk            (get serviceOffer :resource:disk "")
            :instance-type   (get serviceOffer :resource:instanceType "")
            :instance-id     (or instanceID "")
            :connector-href  (get connector :href "")
            :user-href       (get-in deployment [:user :href] "")}) vms)))

(defn vms-table []
  (let [first-button (atom 1)
        number-button-nav 5]
    (fn []
      [sa/Table
       {:compact     "very"
        :size        "small"
        :selectable  true
        :celled      true
        :single-line true
        :padded      false}

       [sa/TableHeader
        (vec (concat [sa/TableRow]
                     (vec (map (fn [label] ^{:key label} [sa/TableHeaderCell label]) vms-table-headers))))]
       (vec (concat [sa/TableBody]
                    (vec (map
                           (fn [entry]
                             (vec (concat [sa/TableRow
                                           {:error (empty? (:deployment-href entry))}] (table-vm-cells entry))))
                           (extract-vms-data @vms-response)))))

       (let [vms-count (get @vms-response :count 0)
             page-count (quot vms-count number-record-display)
             button-range (take number-button-nav (range @first-button (inc page-count)))]
         [sa/TableFooter
          [sa/TableRow
           [sa/TableHeaderCell {:col-span (str (count vms-table-headers))}
            [sa/Label "VMs found" [sa/LabelDetail vms-count]]
            [sa/Menu {:floated "right" :size "mini"}
             [sa/MenuItem {:link true :onClick (fn [e d]
                                                 (reset! activeVmPage 1)
                                                 (reset! first-button 1)
                                                 (reset! vms-loading true)
                                                 (fetch-vms))}
              [sa/Icon {:name "angle double left"}]]
             [sa/MenuItem {:link true :onClick (fn [e d] (when (> @activeVmPage 1)
                                                           (swap! activeVmPage dec)
                                                           (when (< @activeVmPage (first button-range))
                                                             (swap! first-button dec))
                                                           (reset! vms-loading true)
                                                           (fetch-vms)
                                                           ))}
              [sa/Icon {:name "angle left"}]]
             (doall
               (for [i button-range]
                 ^{:key i} [sa/MenuItem {:link    true
                                         :active  (= @activeVmPage i)
                                         :onClick (fn [e d]
                                                    (reset! activeVmPage (.-children d))
                                                    (reset! vms-loading true)
                                                    (fetch-vms)
                                                    )} i]
                 )
               )
             [sa/MenuItem {:link true :onClick (fn [e d] (when (< @activeVmPage page-count)
                                                           (swap! activeVmPage inc)
                                                           (when (> @activeVmPage (last button-range))
                                                             (swap! first-button inc))
                                                           (reset! vms-loading true)
                                                           (fetch-vms)
                                                           ))}
              [sa/Icon {:name "angle right"}]]
             [sa/MenuItem {:link true :onClick (fn [e d]
                                                 (reset! activeVmPage page-count)
                                                 (reset! vms-loading true)
                                                 (reset! first-button
                                                         (let [first-should-be (- page-count (dec number-button-nav))]
                                                           (if (neg-int? first-should-be)
                                                             1 first-should-be)))
                                                 (fetch-vms))}
              [sa/Icon {:name "angle double right"}]]
             ]]]
          ])
       ]
      )))

(defn app []
  (let [tab2-loading @vms-loading]
    [sa/Tab
     {:onTabChange (fn [e, d] (reset! activeTab (.-activeIndex d)))
      :panes       [{:menuItem "Deployments"
                     :render   (fn [] (reagent/as-element [:div {:style {:width "auto" :overflow-x "auto"}}
                                                           [sa/TabPane {:as :div :style {:margin "10px"}}
                                                            [dep/deployments-table]]
                                                           [:br]]))}
                    {:menuItem "Virtual Machines"
                     :render   (fn [] (reagent/as-element
                                        [:div {:style {:width "auto" :overflow-x "auto"}}
                                         [sa/TabPane {:as :div :style {:margin "10px"}} [vms-table]]
                                         [:br]]))}
                    ]}]))

(defn ^:export setCloudFilter [cloud]
  (print "reset offset limit plz")
  (print "All Clouds")
  (print "for all tab")
  (print cloud))

(reagent/render-component [app]
                          (. js/document (getElementById "app")))
