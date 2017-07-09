(ns sixsq.slipstream.webui.panel.app.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.modules :as modules]
    [taoensso.timbre :as log]))

(reg-fx
  :fx.webui.app/search
  (fn [[client url]]
    (go
      (let [results (<! (modules/get-children client url))]
        (log/info (count results) "returned for url" url)
        (dispatch [:set-modules-data results])))))
