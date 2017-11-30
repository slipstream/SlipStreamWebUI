(ns vms.vms
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [sixsq.slipstream.client.api.cimi :as cimi]
            [soda-ash.core :as sa]
            [vms.visibility :as vs]
            [vms.tables-utils :as t]
            [vms.client-utils :as req]
            [clojure.string :as str]))

(enable-console-print!)

(def app-state (atom {:vms              {}
                      :request-opts     {"$first"   0
                                         "$last"    10
                                         "$orderby" "created:desc"}
                      :record-displayed 10
                      :loading          true
                      :headers          ["ID" "State" "IP" "CPU" "RAM [MB]" "DISK [GB]" "Instance type"
                                         "Cloud Instance ID" "Cloud" "Owner"]
                      }))

(defn state-set-loading [v]
  (swap! app-state assoc :loading v))

(defn state-disable-loading []
  (state-set-loading false))

(defn state-enable-loading []
  (state-set-loading true))

(defn state-set-request-opts [k v]
  (swap! app-state assoc-in [:request-opts k] v))

(defn state-set-first [v]
  (state-set-request-opts "$first" v))

(defn state-set-last [v]
  (state-set-request-opts "$last" v))

(defn state-set-filter [v]
  (state-set-request-opts "$filter" v))

(defn state-dissoc-filter []
  (swap! app-state update :request-opts dissoc "$filter"))

(defn state-set-vms [v]
  (swap! app-state assoc :vms v))

(defn vms-count [] (get-in @app-state [:vms :count] 0))

(defn page-count []
  (let [vms-count (vms-count)
        record-displayed (get @app-state :record-displayed)
        full-page-number (quot vms-count record-displayed)
        additionnal-page (if (pos? (mod vms-count record-displayed)) 1 0)]
    (+ full-page-number additionnal-page)))

(defn current-page [] (/ (get-in @app-state [:request-opts "$last"]) (get @app-state :record-displayed)))

(defn set-page [page]
  (let [record-displayed (get @app-state :record-displayed)
        last (* page record-displayed)]
    (state-set-last last)
    (state-set-first (- last record-displayed))))

(defn inc-page []
  (let [current-page (current-page)]
    (when (< current-page (page-count))
      (set-page (inc current-page)))))

(defn dec-page []
  (let [current-page (current-page)]
    (when (> current-page 1)
      (set-page (dec current-page)))))

(defn fetch-vms []
  (go
    (let [response (<! (cimi/search req/client "virtualMachines" (get @app-state :request-opts)))]
      (state-set-vms response)))
  (state-disable-loading))

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
   [sa/TableCell
    {:collapsing true :style {:max-width "50px" :overflow "hidden" :text-overflow "ellipsis"}} instance-type]
   [sa/TableCell
    {:collapsing true :style {:max-width "150px" :overflow "hidden" :text-overflow "ellipsis"}} instance-id]
   [sa/TableCell {:collapsing true :style {:max-width "150px" :overflow "hidden" :text-overflow "ellipsis"}}
    (or (-> connector-href (str/replace #"^connector/" "")) "")]
   [sa/TableCell {:style {:max-width "250px" :overflow "hidden" :text-overflow "ellipsis"}}
    [:a {:href user-href} (str/replace user-href #"^user/" "")]]])

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
      (let [vms (get @app-state :vms)
            headers (get @app-state :headers)]
        [sa/Segment {:basic true :loading (get @app-state :loading)}
         [:p (str (dissoc @app-state :vms))]
         [sa/Table
          {:compact     "very"
           :size        "small"
           :selectable  true
           :celled      true
           :single-line true
           :padded      false}

          [sa/TableHeader
           (vec (concat [sa/TableRow]
                        (vec (map (fn [label] ^{:key label} [sa/TableHeaderCell label]) headers))))]
          (vec (concat [sa/TableBody]
                       (vec (map
                              (fn [entry]
                                (vec (concat [sa/TableRow
                                              {:error (empty? (:deployment-href entry))}] (table-vm-cells entry))))
                              (extract-vms-data vms)))))
          [t/table-navigator-footer
           vms-count
           page-count
           current-page
           #(count (get @app-state :headers))
           #(do (inc-page)
                (state-enable-loading)
                (fetch-vms))
           #(do
              (dec-page)
              (state-enable-loading)
              (fetch-vms))
           #(do (set-page 1)
                (state-enable-loading)
                (fetch-vms))
           #(do
              (set-page (page-count))
              (state-enable-loading)
              (fetch-vms))
           #(do (set-page (.-children %2))
                (state-enable-loading)
                (fetch-vms))]
          ]])
      )))

(defn ^:export setCloudFilter [cloud]
  (if (= cloud "All Clouds")
    (state-dissoc-filter)
    (state-set-filter (str "connector/href=\"connector/" cloud "\"")))
  (state-enable-loading)
  (fetch-vms))


