(ns sixsq.slipstream.webui.routes
  (:require
    [re-frame.core :refer [dispatch]]
    [secretary.core :as secretary :refer-macros [defroute]]
    [sixsq.slipstream.webui.utils :as utils]))

(defroute "/apps" []
          (dispatch [:set-breadcrumbs []]))
(defroute "/apps/*" {path :*}
          (dispatch [:set-breadcrumbs (utils/parse-resource-path path)]))

(defroute "/offer" {query-params :query-params}
          (dispatch [:set-offer query-params]))
(defroute "/offer/:uuid" {uuid :uuid}
          (dispatch [:set-offer-detail uuid]))

(defroute "*" {path :*}
          (dispatch [:set-resource-path path]))
