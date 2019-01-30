(ns sixsq.slipstream.webui.api.effects
  (:require
    [re-frame.core :refer [dispatch reg-fx]]))



(reg-fx
  ::get-templates-description
  (fn [[template-href-key templates]]
    (doseq [[_ {:keys [id operations] :as template}] templates]
      (dispatch [:sixsq.slipstream.webui.api.events/get-template-description template-href-key id]))))
