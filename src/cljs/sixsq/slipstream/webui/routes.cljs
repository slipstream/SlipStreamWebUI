(ns sixsq.slipstream.webui.routes
  (:require
    [re-frame.core :refer [dispatch]]
    [secretary.core :as secretary :refer-macros [defroute]]))

(defroute "/*" {path :* query-params :query-params}
          (dispatch [:evt.webui.authn/set-error-message (:error query-params)])
          (dispatch [:evt.webui.main/set-resource-path path query-params]))

(defroute "*" {path :*}
          (dispatch [:evt.webui.main/set-resource-path path]))
