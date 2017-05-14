(ns sixsq.slipstream.webui.widget.i18n.views
  (:require
    [re-com.core :refer [single-dropdown]]
    [re-frame.core :refer [subscribe dispatch]]
    [sixsq.slipstream.webui.widget.i18n.dictionary :refer [dictionary]]
    [sixsq.slipstream.webui.widget.i18n.events]
    [sixsq.slipstream.webui.widget.i18n.subs]))

(defn get-locale-label
  [locale]
  (or (get-in dictionary [locale :lang])
      (name locale)))

(defn locale-choice
  [locale]
  {:id (name locale)
   :label (get-locale-label locale)})

(defn locale-choices []
  (doall (->> dictionary
              keys
              (map locale-choice)
              (sort-by :label)
              vec)))

(defn locale-selector
  []
  (let [locale (subscribe [:webui.i18n/locale])]
    (fn []
      [single-dropdown
       :model @locale
       :width "100px"
       :choices (locale-choices)
       :on-change #(dispatch [:evt.webui.i18n/set-locale %])])))

