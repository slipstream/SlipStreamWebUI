(ns sixsq.slipstream.webui.authn.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(defn logged-in?
  [db _]
  (get-in db [:authn :logged-in?]))
(reg-sub :logged-in? logged-in?)

(defn authn
  [db _]
  (:authn db))
(reg-sub :authn authn)

