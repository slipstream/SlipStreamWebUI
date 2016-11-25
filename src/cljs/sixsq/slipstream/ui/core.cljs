(ns sixsq.slipstream.ui.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [clojure.walk :as w]
    [reagent.core :as r]
    [reagent-modals.modals :as reagent-modals]
    [reagent-forms.core :as forms]
    [reforms.reagent :include-macros true :as f]
    [reforms.table :as t]
    [cljs.core.async :refer [<!]]
    [cljs.pprint :refer [pprint]]
    [cljsjs.react-bootstrap]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.api.cimi.async :as cimi-async]))

(enable-console-print!)

(def Button (r/adapt-react-class (aget js/ReactBootstrap "Button")))
(def ButtonGroup (r/adapt-react-class (aget js/ReactBootstrap "ButtonGroup")))

(def ^cimi/cimi client (delay (cimi-async/instance)))

;; --------------------------------------------------
;; reforms tests
;; --------------------------------------------------
(def some-data (r/atom {}))

(defn simple-view
  [data]
  (f/panel
   "Login"
   (f/form
    (f/text data [:username] :placeholder "username")
    (f/password data [:password] :placeholder "password")
    (f/form-buttons
     (f/button-primary "login" #(js/alert (:username @data) ))))))


(def some-table (r/atom {}))
(defn simple-table
  [data]
  (t/table {:key "rs-table"
            :class "table-striped"}
           [{:name "Tom" :id 1} {:name "Jerry" :id 2} {:name "Mickey" :id 3} {:name "Minnie" :id 4}]
           :columns {:name "Hero name"}
           :checkboxes {:selection data
                        :path [:selected]
                        :row-id :id}))

;; --------------------------------------------------
;; Cloud Entry Point
;; --------------------------------------------------
(def cep (r/atom nil))

(defn clear-cep [& _]
  (swap! cep (constantly nil)))

(defn load-cep [& _]
  (go
    (let [resp (<! (cimi/cloud-entry-point @client))]
      (println "Loading CloudEntryPoint!")
      (swap! cep (constantly (w/keywordize-keys resp))))))

(defn cep-widget []
  [:div
   [:h1 "Cloud Entry Point"]
   [:div
    [Button {:on-click load-cep} "load"]
    [Button {:on-click clear-cep} "clear"]]
   [:pre (with-out-str (pprint (or @cep "__empty__")))]])

(def common-keys #{:baseURI :id :acl :resourceURI :created :updated})

(defn format-link [data k]
  (let [base-uri (:baseURI data)
        url (str base-uri (:href (k data)))]
    [k (str (name k) " " url)]))

(defn cep-select [data]
  (let [base-uri (:baseURI @data)
        ks (sort (remove common-keys (keys @data)))
        opts (map (partial format-link @data) ks)]
    (f/select "Select" data [:select]
              opts)))


;; --------------------------------------------------
;; Login Widget
;; --------------------------------------------------
(def token (r/atom nil))
(def username (r/atom ""))
(def password (r/atom ""))
(def login-data (r/atom {}))

(defn login [& _]
  (go
    (let [resp (<! (cimi/login @client {:username @username, :password @password}))]
      (println "Logging in as " @username "!")
      (println "RESPONSE: " resp)
      (swap! token (constantly "OK")))))

(defn logout [& _]
  (swap! token (constantly nil)))

(defn login-widget []
  [:div
   [:form
    [:label "username"
     [:input {:type      "text"
              :value     @username
              :on-change #(reset! username (-> % .-target .-value))}]]
    [:label "password"
     [:input {:type      "password"
              :value     @password
              :on-change #(reset! password (-> % .-target .-value))}]]]
   [:div
    [Button {:on-click login} "login"]
    [Button {:on-click logout} "logout"]]
   [:p "TOKEN: " @token]])

(defn login-x [data]
  (let [{:keys [username password]} @data]
    (go
     (let [resp (<! (cimi/login @client {:username username,
                                         :password password}))]
       (println "Logging in as " username "!")
       (println "RESPONSE: " resp)
       (let [update (assoc @data :logged-in (= 200 (:login-status resp)))]
         (swap! data (constantly update)))))))

(defn logout-x [data]
  (go
   (let [resp (<! (cimi/logout @client))]
      (println "Logging out")
      (println "RESPONSE: " resp)
      (let [update (-> @data
                       (assoc :logged-in false)
                       (dissoc :password)
                       (dissoc :username))]
        (swap! data (constantly update))))))

(defn login-panel
  [data]
  (if (:logged-in @data)
    (f/panel
     "Logout"
     (f/form
      (f/button-primary "logout" #(logout-x data))))
    (f/panel
     "Login"
     (f/form
      (f/text data [:username] :placeholder "username")
      (f/password data [:password] :placeholder "password")
      (f/form-buttons
       (f/button-primary "login" #(login-x data)))))
    ))

(defn login-modal [data]
  (f/panel
    "Login"
    (f/form
      (f/text data [:username] :placeholder "username")
      (f/password data [:password] :placeholder "password")
      (f/form-buttons
        (f/button-primary "login" #(login-x data))))))

(defn fake-login [data]
  (let [update (-> @data
                   (assoc :username "my-username")
                   (assoc :logged-in true)
                   (dissoc :password))]
    (login-modal data)
    (swap! data (constantly update))))

(defn fake-logout [data]
  (let [update (-> @data
                   (dissoc :username)
                   (dissoc :password)
                   (assoc :logged-in false))]
    (swap! data (constantly update))))

(defn logged-out [data]
  [:div
   [Button {:on-click #(fake-login data)} "login"]])

(defn logged-in [data]
  (let [{:keys [username]} @data]
    [:div
     [ButtonGroup
      [Button {:on-click #(js/alert (str "PROFILE: " username))} username]
      [Button {:on-click #(fake-logout data)} "logout"]]]))

(defn login-area [data]
  (if (:logged-in @data)
    (logged-in data)
    (logged-out data)))

(defn home []
  [:div
   [reagent-modals/modal-window]
   ])

(defn login-form []
  [:div
   [:h3 "Options"]
   [:div.form-group
    [:select.form-control {:field :list :id :many.options}
     [:option {:key :foo} "foo"]
     [:option {:key :bar} "bar"]
     [:option {:key :baz} "baz"]]
    [:input.form-control {:field :text :id :username}]
    [:input.form-control {:field :password :id :password}]
    [:div.btn.btn-primary nil "login"]]])

(defn modal-window-button [data]
  [:div.btn.btn-primary
   {:on-click #(reagent-modals/modal! (simple-view data))}
   "My Modal"])

;; --------------------------------------------------
;; Events
;; --------------------------------------------------
(def events (r/atom nil))

(defn clear-events [& _]
  (swap! events (constantly nil)))

(defn load-events [& _]
  (go
    (if (and @token @cep)
      (let [resp (<! (cimi/search @client "events"))]
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
  (r/render [login-area login-data]
            (js/document.getElementById "login-area"))
  (r/render [login-widget]
            (js/document.getElementById "login"))
  (r/render [cep-widget]
            (js/document.getElementById "cep"))
  (r/render [events-widget]
            (js/document.getElementById "events"))
  (r/render [login-panel some-data]
            (js/document.getElementById "reform"))
  (r/render [simple-table some-table]
            (js/document.getElementById "table"))
  (r/render [cep-select cep]
            (js/document.getElementById "select"))
  (r/render [home]
            (js/document.getElementById "home"))
  (r/render [modal-window-button login-data]
            (js/document.getElementById "modal"))
  )
