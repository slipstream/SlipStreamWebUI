(ns sixsq.slipstream.webui.dashboard.views-vms
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
    [sixsq.slipstream.webui.dashboard.subs :as dashbord-subs]))

(def app-state (r/atom {:vms              {}
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
  (let [vc (vms-count)
        record-displayed (get @app-state :record-displayed)
        full-page-number (quot vc record-displayed)
        additionnal-page (if (pos? (mod vc record-displayed)) 1 0)]
    (+ full-page-number additionnal-page)))

(defn current-page [] (/ (get-in @app-state [:request-opts "$last"]) (get @app-state :record-displayed)))

(defn set-page [page]
  (let [record-displayed (get @app-state :record-displayed)
        last (* page record-displayed)]
    (state-set-last last)
    (state-set-first (- last record-displayed))))

(defn inc-page []
  (let [cp (current-page)]
    (when (< cp (page-count))
      (set-page (inc cp)))))

(defn dec-page []
  (let [cp (current-page)]
    (when (> cp 1)
      (set-page (dec cp)))))

;(defn fetch-vms []
;  (go
;    (let [response (<! (cimi/search client/client "virtualMachines" (get @app-state :request-opts)))]
;      (state-set-vms response)))
;  (state-disable-loading))

(defn table-vm-cells [{:keys [deployment-href state ip vcpu ram disk instance-type instance-id connector-href
                              user-href]}]
  [[ui/TableCell {:collapsing true}
    (when (empty? deployment-href)
      [ui/Popup {:trigger  (r/as-element [:div [ui/Icon {:name "exclamation circle"}] "Unkown"])
                 :inverted true
                 :size     "mini" :content "Deployment UUID unknown" :position "left center"}])
    [:a {:href deployment-href} (or (-> deployment-href
                                        (str/replace #"^run/" "")
                                        (str/split #"-")
                                        (first)) "")]]
   [ui/TableCell {:collapsing true} state]
   [ui/TableCell {:collapsing true} ip]
   [ui/TableCell {:collapsing true :textAlign "center"} vcpu]
   [ui/TableCell {:collapsing true :textAlign "center"} ram]
   [ui/TableCell {:collapsing true :textAlign "center"} disk]
   [ui/TableCell
    {:collapsing true :style {:max-width "50px" :overflow "hidden" :text-overflow "ellipsis"}} instance-type]
   [ui/TableCell
    {:collapsing true :style {:max-width "150px" :overflow "hidden" :text-overflow "ellipsis"}} instance-id]
   [ui/TableCell {:collapsing true :style {:max-width "150px" :overflow "hidden" :text-overflow "ellipsis"}}
    (or (-> connector-href (str/replace #"^connector/" "")) "")]
   [ui/TableCell {:style {:max-width "250px" :overflow "hidden" :text-overflow "ellipsis"}}
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
  (let [vms (subscribe [::dashbord-subs/virtual-machines])
        headers ["ID" "State" "IP" "CPU" "RAM [MB]" "DISK [GB]" "Instance type" "Cloud Instance ID" "Cloud" "Owner"]]
    (fn []
      [ui/Segment {:basic true :loading false #_(get @app-state :loading)}
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
                      (vec (map (fn [label] ^{:key label} [ui/TableHeaderCell label]) headers))))]
        (vec (concat [ui/TableBody]
                     (vec (map
                            (fn [entry]
                              (vec (concat [ui/TableRow
                                            {:error (empty? (:deployment-href entry))}] (table-vm-cells entry))))
                            (extract-vms-data vms)))))
        #_[t/table-navigator-footer
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
        ]])))