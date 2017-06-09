(ns sixsq.slipstream.webui.routes
  (:require
    [re-frame.core :refer [dispatch]]
    [secretary.core :as secretary :refer-macros [defroute]]
    [sixsq.slipstream.webui.utils :as utils]
    [taoensso.timbre :as log]))

(defroute "/app" []
          (dispatch [:set-breadcrumbs []]))
(defroute "/app/*" {path :*}
          (dispatch [:set-breadcrumbs (utils/parse-resource-path path)]))

(defroute "/offer" {query-params :query-params}
          (dispatch [:set-offer query-params]))
(defroute "/offer/:uuid" {uuid :uuid}
          (dispatch [:set-offer-detail uuid]))

(defroute "/login" {query-params :query-params}
          (dispatch [:set-login-path-and-error (:error query-params)]))

(defroute "*" {path :*}
          (dispatch [:set-resource-path path]))
