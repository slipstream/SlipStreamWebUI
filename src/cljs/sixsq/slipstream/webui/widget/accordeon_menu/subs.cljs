(ns sixsq.slipstream.webui.widget.accordeon-menu.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :name
 (fn [db]
   (:name db)))

(rf/reg-sub
  :main-menu/display
  (fn [db]
    (:main-menu/display db)))
