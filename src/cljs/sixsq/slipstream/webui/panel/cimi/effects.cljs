(ns sixsq.slipstream.webui.panel.cimi.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.panel.credential.utils :as utils]
    [taoensso.timbre :as log]))

(reg-fx
  :fx.webui.cimi/get-description
  (fn [[template]]
    (go
      (when-let [description (<! (utils/complete-parameter-description template))]
        (dispatch [:evt.webui.cimi/set-description description])))))

(reg-fx
  :fx.webui.cimi/get-templates
  (fn [[client collection-keyword]]
    (go
      (when-let [results (<! (utils/get-templates client collection-keyword))]
        (doseq [result results]
          (dispatch [:evt.webui.cimi/get-description result]))))))

(defn format-field [[k v]]
  [:div [:h2 k] [:pre v]])

(defn other-fields-hiccup [response]
  (into [:div] (vec (map format-field (remove #(contains? #{:status :message :resource-id} (first %)) response)))))

;; FIXME: Problem here with hardcoded credentials!  All creation requests post to :credentials!
(reg-fx
  :fx.webui.cimi/create-resource
  (fn [[client request-body]]
    (go
      (let [{:keys [status message resource-id] :as response} (<! (cimi/add client :credentials request-body))]
        (let [other-fields (other-fields-hiccup response)
              alert-type (if (= 201 status) :info :danger)
              heading (if (= 201 status) "Success" "Failure")
              body [:div [:p message] other-fields]
              alert {:alert-type alert-type
                     :heading heading
                     :body body}]
          (dispatch [:evt.webui.main/raise-alert {:alert-type alert-type
                                                  :heading heading
                                                  :body body}]))))))
