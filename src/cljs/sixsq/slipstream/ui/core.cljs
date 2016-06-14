(ns sixsq.slipstream.ui.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [clojure.walk :as w]
   [reagent.core :as r]
   [cljs.core.async :refer [<!]]
   [cljs.pprint :refer [pprint]]
   [cljsjs.react-bootstrap]
   [sixsq.slipstream.client.api.authn :as authn]
   [sixsq.slipstream.client.api.cimi-async :as cimi]))

(enable-console-print!)

(def Button (r/adapt-react-class (aget js/ReactBootstrap "Button")))

;; --------------------------------------------------
;; Cloud Entry Point
;; --------------------------------------------------
(def cep (r/atom nil))

(defn clear-cep [& _]
  (swap! cep (constantly nil)))

(defn load-cep [& _]
  (go
   (let [resp (<! (cimi/cloud-entry-point))]
     (println "Loading CloudEntryPoint!")
     (swap! cep (constantly (w/keywordize-keys resp))))))
    
(defn cep-widget []
  [:div
   [:h1 "Cloud Entry Point"]
   [:div
    [Button {:on-click load-cep} "load"]
    [Button {:on-click clear-cep} "clear"]]
   [:pre (with-out-str (pprint (or @cep "__empty__")))]])
    
;; --------------------------------------------------
;; Login Widget
;; --------------------------------------------------
(def token (r/atom nil))
(def username (r/atom ""))
(def password (r/atom ""))

(defn login [& _]
  (go
   (let [resp (<! (authn/login-async @username @password))]
     (println "Logging in as " @username "!")
     (println "RESPONSE: " resp)
     (swap! token (constantly "OK")))))

(defn logout [& _]
  (swap! token (constantly nil)))

(defn login-widget []
  [:div
   [:form
    [:label "username" 
     [:input {:type "text"
              :value @username
              :on-change #(reset! username (-> % .-target .-value))}]]
    [:label "password"
     [:input {:type "password"
              :value @password
              :on-change #(reset! password (-> % .-target .-value))}]]]
   [:div
    [Button {:on-click login} "login"]
    [Button {:on-click logout} "logout"]]
   [:p "TOKEN: " @token]])

;; --------------------------------------------------
;; Events
;; --------------------------------------------------
(def events (r/atom nil))

(defn clear-events [& _]
  (swap! events (constantly nil)))

(defn load-events [& _]
  (go
   (if (and @token @cep)
     (let [cep-clj (js->clj @cep {:keywordize-keys true})
           resp (<! (cimi/search @token cep-clj "events"))]
       (println "Loading Events!")
       (let [event-count (count (get resp "events"))]
         (swap! events (constantly event-count)))))))
    
(defn events-widget []
  [:div
   [:h1 "Events"]
   [:div
    [Button {:on-click load-events} "load"]
    [Button {:on-click clear-events} "clear"]]
   [:pre (with-out-str (pprint (or @events "__empty__")))]])
    
;; --------------------------------------------------
;; Core Page
;; --------------------------------------------------
(defn ^:export run []
  (r/render [login-widget]
            (js/document.getElementById "login"))
  (r/render [cep-widget]
            (js/document.getElementById "cep"))
  (r/render [events-widget]
            (js/document.getElementById "events")))
