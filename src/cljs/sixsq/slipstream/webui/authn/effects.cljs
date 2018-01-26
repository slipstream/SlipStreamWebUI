(ns sixsq.slipstream.webui.authn.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]
    [taoensso.timbre :as log]))

(reg-fx
  ::initialize
  (fn [[client callback]]
    (go (callback (<! (cimi-api-utils/extract-template-info client))))))

(reg-fx
  ::process-template
  (fn [[tpl callback]]
    (go
      (if-let [prepared-template (<! (cimi-api-utils/complete-parameter-description tpl))]
        (callback prepared-template)))))
