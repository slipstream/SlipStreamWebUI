(ns sixsq.slipstream.webui.widget.history.effects
  (:require
    [re-frame.core :refer [reg-fx]]
    [sixsq.slipstream.webui.widget.history.utils :as hutils]))

;; initialize the history with first URL
(reg-fx
  :history/initialize
  (fn [[path-prefix]]
    (hutils/initialize path-prefix)
    (hutils/start)))

;; usage: (dispatch [:navigate url])
;; navigates to the given url, which must include the 'panel' type
(reg-fx
  :navigate
  (fn [[url]]
    (hutils/navigate url)))
