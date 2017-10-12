(ns sixsq.slipstream.webui.panel.cimi.utils
  (:require
    [re-frame.core :refer [dispatch]]
    [cljs.pprint :refer [pprint]]
    [taoensso.timbre :as log]))

(defn collection-href-map
  "Creates a map from the CloudEntryPoint that maps the resource collection
   key (as a keyword) to the href for the collection (as a string)."
  [cep]
  (when cep
    (into {} (->> cep
                  (map (juxt first #(:href (second %))))
                  (remove (fn [[k v]] (not (string? v))))))))

(defn collection-key-map
  "Creates a map from the CloudEntryPoint that maps the collections href (as a
   string) to the key for the collection (as a keyword)."
  [cep]
  (when cep
    (into {} (->> cep
                  collection-href-map
                  (map (juxt second first))))))

(defn dispatch-alert-document
  [result]
  (let [[alert-type heading] [:info "Response"]
        body [:pre (with-out-str (cljs.pprint/pprint result))]
        alert {:alert-type alert-type
               :heading    heading
               :body       body}]
    (dispatch [:evt.webui.main/raise-alert alert])))

(defn dispatch-alert
  [result]
  (let [{:keys [status message]} result
        [alert-type heading] (if (<= 200 status 299) [:info "Success"] [:danger "Failure"])
        body [:p message]
        alert {:alert-type alert-type
               :heading    heading
               :body       body}]
    (dispatch [:evt.webui.main/raise-alert alert])))

(defn dispatch-alert-on-error [data]
  (if (instance? js/Error data)
    (let [error-body (or (ex-data data) (str data))
          alert-type :danger
          heading "Failure"
          body [:pre (with-out-str (pprint error-body))]
          alert {:alert-type alert-type
                 :heading    heading
                 :body       body}]
      (dispatch [:evt.webui.main/raise-alert alert])
      true)
    false))

(defn dispatch-edit-alert [response]
  (if (instance? js/Error response)
    (let [error-body (:body (ex-data response))
          alert-type :danger
          heading "Failure"
          body [:pre (with-out-str (pprint error-body))]
          alert {:alert-type alert-type
                 :heading    heading
                 :body       body}]
      (dispatch [:evt.webui.main/raise-alert alert]))
    (let [alert-type :info
          heading "Success"
          body [:p (with-out-str (pprint response))]
          alert {:alert-type alert-type
                 :heading    heading
                 :body       body}]
      (dispatch [:evt.webui.main/raise-alert alert]))))
