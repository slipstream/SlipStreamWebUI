(ns sixsq.slipstream.webui.routes
  (:require
    [re-frame.core :refer [dispatch]]
    [secretary.core :as secretary :refer-macros [defroute]]
    [sixsq.slipstream.webui.utils :as utils]))

(defroute "/*" {path :* query-params :query-params}
          (dispatch [:evt.webui.authn/set-error-message (:error query-params)])
          (dispatch [:set-resource (utils/parse-resource-path path) query-params]))

(defroute "*" {path :*}
          (dispatch [:set-resource-path path]))
