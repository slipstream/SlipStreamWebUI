(ns sixsq.slipstream.webui.panel.cimi.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]))

;; usage: (dispatch [:search client resource-type])
;; queries the given resource
(reg-fx
  :fx.webui.cimi/search
  (fn [[client resource-type params]]
    (go
      (let [results (<! (cimi/search client resource-type params))]
        (dispatch [:show-search-results resource-type results])))))

(reg-fx
  :fx.webui.cimi/delete
  (fn [[client resource-id]]
    (go
      (let [{:keys [status message]} (<! (cimi/delete client resource-id))
            [alert-type heading] (if (<= 200 status 299) [:info "Success"] [:danger "Failure"])
            body [:p message]
            alert {:alert-type alert-type
                   :heading    heading
                   :body       body}]
        (dispatch [:evt.webui.main/raise-alert {:alert-type alert-type
                                                :heading    heading
                                                :body       body}])))))

(reg-fx
  :fx.webui.cimi/edit
  (fn [[client resource-id data]]
    (go
      (if (instance? js/Error data)
        (let [error-body (or (ex-data data) (str data))
              alert-type :danger
              heading "Failure"
              body [:pre (with-out-str (cljs.pprint/pprint error-body))]
              alert {:alert-type alert-type
                     :heading    heading
                     :body       body}]
          (dispatch [:evt.webui.main/raise-alert {:alert-type alert-type
                                                  :heading    heading
                                                  :body       body}]))
        (let [response (<! (cimi/edit client resource-id data))]
          (if (instance? js/Error response)
            (let [error-body (:body (ex-data response))
                  alert-type :danger
                  heading "Failure"
                  body [:pre (with-out-str (cljs.pprint/pprint error-body))]
                  alert {:alert-type alert-type
                         :heading    heading
                         :body       body}]
              (dispatch [:evt.webui.main/raise-alert {:alert-type alert-type
                                                      :heading    heading
                                                      :body       body}]))
            (let [alert-type :info
                  heading "Success"
                  body [:p (with-out-str (cljs.pprint/pprint response))]
                  alert {:alert-type alert-type
                         :heading    heading
                         :body       body}]
              (dispatch [:evt.webui.main/raise-alert {:alert-type alert-type
                                                      :heading    heading
                                                      :body       body}]))))))))
