(ns sixsq.slipstream.webui.main.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sixsq.slipstream.webui.main.spec :as main-spec]))


(reg-sub
  ::sidebar-open?
  ::main-spec/sidebar-open?)


(reg-sub
  ::visible?
  ::main-spec/visible?)


(reg-sub
  ::nav-path
  ::main-spec/nav-path)


(reg-sub
  ::nav-query-params
  ::main-spec/nav-query-params)
