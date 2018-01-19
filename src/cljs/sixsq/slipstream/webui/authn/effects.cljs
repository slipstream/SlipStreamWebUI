(ns sixsq.slipstream.webui.authn.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.webui.authn.utils :as au]
    [taoensso.timbre :as log]))

(reg-fx
  ::initialize
  (fn [[client callback]]
    (go (callback (<! (au/extract-template-info client))))))

(reg-fx
  ::process-template
  (fn [[tpl callback]]
    (go
      (if-let [prepared-template (<! (au/complete-parameter-description tpl))]
        (callback prepared-template)))))
