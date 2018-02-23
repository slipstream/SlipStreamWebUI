(ns sixsq.slipstream.webui.deployment-detail.views
  (:require
    [clojure.string :as str]
    [taoensso.timbre :as log]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [reagent.core :as r]

    [sixsq.slipstream.webui.utils.component :as ui-utils]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]

    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.deployment-detail.events :as deployment-detail-events]
    [sixsq.slipstream.webui.deployment-detail.subs :as deployment-detail-subs]))

(defn ^:export set-runUUID [uuid]
  (log/debug "dispatch set-runUUID: " uuid)
  (dispatch [::deployment-detail-events/set-runUUID uuid]))

(defn summary-section
  []
  (let [runUUID (subscribe [::deployment-detail-subs/runUUID])]
    (fn []
      [:div @runUUID])))

(defn report-item
  [{:keys [id component created state] :as report}]
  ^{:key id} [:li
              (let [label (str/join " " [component created])]
                (if (= state "ready")
                 [:a {:onClick #(dispatch [::deployment-detail-events/download-report id])} label]
                 label))])

(defn reports-section
  []
  (let [runUUID (subscribe [::deployment-detail-subs/runUUID])
        reports (subscribe [::deployment-detail-subs/reports])
        on-click #(dispatch [::deployment-detail-events/fetch-reports])]
    (dispatch [::deployment-detail-events/fetch-reports])
    (dispatch [::main-events/action-interval
               {:action    :start
                :id        :deployment-detail-reports
                :frequency 30000
                :event     [::deployment-detail-events/fetch-reports]}])
    (fn []
      [:div
       (vec
         (concat [:ul]
                 (vec (map report-item (:externalObjects @reports)))))
       [:p "Reports will be displayed as soon as available. No need to refresh."]
       ])))

(defn deployment-detail
  []
  (let [tr (subscribe [::i18n-subs/tr])
        active-index (r/atom 1)
        sections [[:summary summary-section]
                  [:reports reports-section]]
        on-click (ui-utils/callback :index #(reset! active-index
                                                    (if (= @active-index %) -1 %)))]
    (fn []
      [:div
       (vec
         (concat
           [ui/Accordion {:styled true :fluid true}]
           (into [] cat (map
                          (fn [i [section-title section-content]]
                            ^{:key (str i "_" section-title)}
                            [[ui/AccordionTitle {:onClick on-click :index i}
                              [ui/Icon {:name "dropdown"}]
                              (@tr [section-title])]
                             [ui/AccordionContent {:active (= @active-index i)}
                              [section-content]]]) (range) sections))))])))
