(ns sixsq.slipstream.webui.application.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.application.spec :as application-spec]))


(reg-sub
  ::completed?
  ::application-spec/completed?)


(reg-sub
  ::module-id
  ::application-spec/module-id)


(reg-sub
  ::module
  ::application-spec/module)
