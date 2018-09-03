(ns sixsq.slipstream.webui.quota.views
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.quota.events :as quota-events]
    [sixsq.slipstream.webui.quota.subs :as quota-subs]
    [sixsq.slipstream.webui.client.subs :as client-subs]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]))



(defn control-bar []
  (let [tr (subscribe [::i18n-subs/tr])]
    (dispatch [::quota-events/get-quotas])
    (fn []
      [:div
       [ui/Menu {:attached "top", :borderless true}
        [uix/MenuItemWithIcon
         {:name      (@tr [:refresh])
          :icon-name "refresh"
          ;:on-click  #(dispatch [::usage-events/fetch-meterings])
          }]]])))

(defn fancy-quota-name
  [{:keys [resource aggregation name]}]
  (if (= resource "VirtualMachine")
    (case aggregation
      "count:id" "VMs"
      "sum:serviceOffer/resource:vcpu" "CPUs"
      "sum:serviceOffer/resource:ram" "RAM"
      "sum:serviceOffer/resource:disk" "Disk"
      name)
    name))

(defn get-color
  [quota-value limit]
  (let [progress (/ quota-value limit)]
    (cond
      (< progress 0.20) "green"
      (< progress 0.40) "olive"
      (< progress 0.60) "yellow"
      (< progress 0.80) "orange"
      true "red")))

(defn quota-view [{:keys [id] :as quota}]
  (let [quota-value (reagent/atom "-")
        client (subscribe [::client-subs/client])
        set-value #(reset! quota-value (get % :currentUser "-"))]
    (go
      (set-value (<! (cimi/operation @client id "http://sixsq.com/slipstream/1/action/collect"))))
    (fn [{:keys [name description limit] :as quota}]
      [ui/Popup
       {:header   name
        :content  description
        :position "top center"
        :trigger  (reagent/as-element
                    [ui/Progress
                     (cond-> {:value     @quota-value
                              :size      "small"
                              :total     limit
                              :progress  "value"
                              :label     (str (fancy-quota-name quota) " [" @quota-value "/" limit "]")}
                             (number? @quota-value) (assoc :color (get-color @quota-value limit)))])}])))

(defn quota-view-comp
  [{id :id :as quota}]
  ^{:key id} [quota-view quota])

(defn credential-view [selection credential-quotas]
  ^{:key selection}
  (vec
    (concat [cc/collapsible-card
             selection]
            (map quota-view-comp credential-quotas))))

(defn page-count [record-displayed element-count]
  (cond-> element-count
          true (quot record-displayed)
          (pos? (mod element-count record-displayed)) inc))

(defn search-result []
  (let [loading? (subscribe [::quota-subs/loading?])
        credentials-quotas-map (subscribe [::quota-subs/credentials-quotas-map])
        active-page (reagent/atom 1)
        element-displayed 10
        set-page #(reset! active-page %)]
    (fn []
      (let [elements-count (count @credentials-quotas-map)
            start-slice (* (dec @active-page) element-displayed)
            end-slice (let [end (* @active-page element-displayed)]
                        (if (> end elements-count)
                          elements-count
                          end))]
        (vec (concat [ui/Segment (merge style/autoscroll-x {:loading @loading?})]
                     (map credential-view
                          (subvec (vec (keys @credentials-quotas-map))
                                  start-slice
                                  end-slice)
                          (subvec (vec (vals @credentials-quotas-map))
                                  start-slice
                                  end-slice))
                     [[ui/Pagination {:size         "tiny"
                                      :totalPages   (page-count element-displayed elements-count)
                                      :activePage   @active-page
                                      :onPageChange (ui-callback/callback :activePage set-page)
                                      }]]))))))

(defn quota
  []
  [ui/Container {:fluid true}
   [control-bar]
   [search-result]])

(defmethod panel/render :quota
  [_]
  [quota])
