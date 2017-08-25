(ns sixsq.slipstream.webui.widget.editor.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :webui.editor/data
  (fn [db _] (or (-> db :editor :data) "")))
