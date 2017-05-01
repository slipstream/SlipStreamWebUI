(ns sixsq.slipstream.webui.panel.apps.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.modules :as modules]
    [sixsq.slipstream.webui.history :as history]))

;; usage: (dispatch [:modules-search client])
;; queries the given resource
(reg-fx
  :modules/search
  (fn [[client url]]
    (go
      (let [results (<! (modules/get-children client url))]
        (dispatch [:set-modules-data results])))))

;; usage: (dispatch [:navigate url])
;; navigates to the given url, which must include the 'panel' type
(reg-fx
  :navigate
  (fn [[url]]
    (history/navigate url)))
