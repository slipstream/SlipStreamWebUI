(ns sixsq.slipstream.legacy.components
  (:require [sixsq.slipstream.legacy.components.dashboard-tabs :as dashboard-tabs]
            [sixsq.slipstream.legacy.components.dashboard-tabs.deployments :as deployments]
            [sixsq.slipstream.legacy.components.metering :as metering]))

(defn render-component-when-present [tag component-init-fn]
      (when-let [container-element (.getElementById js/document tag)]
                (component-init-fn container-element)))

(defn ^:export init []
      (render-component-when-present "dashboard-tabs-container" dashboard-tabs/init)
      (render-component-when-present "metering-container" metering/init)
      (render-component-when-present "deployments-container" deployments/init))