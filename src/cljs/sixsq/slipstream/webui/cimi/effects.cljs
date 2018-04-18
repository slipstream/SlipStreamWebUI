(ns sixsq.slipstream.webui.cimi.effects
  (:require
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.webui.cimi-api.effects :as cimi-api-fx]
    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]
    [taoensso.timbre :as log]))



(reg-fx
  ::get-templates-description
  (fn [[template-href-key templates]]
    (doseq [[_ {:keys [id operations] :as template}] templates]
      (dispatch [:sixsq.slipstream.webui.cimi.events/get-template-description template-href-key id]))))