(ns sixsq.slipstream.webui.i18n.views
  (:require
    [re-com.core :refer [single-dropdown]]
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.i18n.dictionary :refer [dictionary]]
    [sixsq.slipstream.webui.i18n.events]
    [sixsq.slipstream.webui.i18n.subs]))

(defn get-lang
  [k]
  (if-let [{:keys [lang]} (get dictionary k)]
    lang
    (name k)))

(defn locale-choice
  [k]
  {:id (name k) :label (get-lang k)})

(def local-choice-comparator
  (comparator (fn [x y] (< (:label x) (:label y)))))

(defn locale-choices []
  (doall (->> dictionary
              keys
              (map locale-choice)
              (sort local-choice-comparator)
              vec)))

(defn locale-selector
  []
  (let [locale (subscribe [:i18n-locale])]
    (fn []
      [single-dropdown
       :model @locale
       :width "100px"
       :choices (locale-choices)
       :on-change (fn [id]
                    (dispatch [:set-locale id]))])))

