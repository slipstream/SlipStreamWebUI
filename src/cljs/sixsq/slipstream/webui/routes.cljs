(ns sixsq.slipstream.webui.routes
  (:require
    [re-frame.core :refer [dispatch]]
    [secretary.core :as secretary :refer-macros [defroute]]
    [sixsq.slipstream.webui.utils :as utils]))

#_(defroute "/app" []
            (dispatch [:set-breadcrumbs []]))
#_(defroute "/app/*" {path :*}
            (dispatch [:set-breadcrumbs (utils/parse-resource-path path)]))

#_(defroute "/offer" {query-params :query-params}
            (dispatch [:set-offer query-params]))
#_(defroute "/offer/:uuid" {uuid :uuid}
            (dispatch [:set-offer-detail uuid]))

#_(defroute "/login" {query-params :query-params}
            (dispatch [:set-login-path-and-error (:error query-params)]))

(defroute "/*" {path :* query-params :query-params}
          (dispatch [:set-resource (utils/parse-resource-path path) query-params]))

(defroute "*" {path :*}
          (dispatch [:set-resource-path path]))
