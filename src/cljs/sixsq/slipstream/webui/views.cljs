(ns sixsq.slipstream.webui.views
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [subscribe dispatch]]))

(defn message
  []
  (let [message (subscribe [:message])]
    (fn []
      [:div
       (if @message [:span @message])])))

(defn logout
  []
  (let [authn (subscribe [:authn])]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if logged-in?
          [:div
           [:button {:on-click #(js/alert (str "profile for " user-id))} user-id]
           [:button {:on-click #(dispatch [:logout])} "logout"]])))))

(defn login
  []
  (let [authn (subscribe [:authn])
        username (reagent/atom "username")
        password (reagent/atom "password")]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if-not logged-in?
          [:div
           [:input {:type       "text"
                    :auto-focus true
                    :on-change  #(reset! username (-> % .-target .-value))}]
           [:input {:type       "password"
                    :auto-focus true
                    :on-change  #(reset! password (-> % .-target .-value))}]
           [:button {:on-click #(dispatch [:login {:username @username :password @password}])} "login"]])))))

(defn authn-panel []
  [:div [login] [logout]])

(def common-keys
  #{:id :created :updated :acl :baseURI :resourceURI})

(defn format-link [k]
  [:option {:value (name k)} (name k)])

(defn cep-select [cep]
  (let [ks (sort (remove common-keys (keys cep)))
        opts (map format-link ks)]
    [:select {:on-change #(dispatch [:switch-search-resource (-> % .-target .-value)])} opts]))

(defn cloud-entry-point
  []
  (let [cep (subscribe [:cloud-entry-point])]
    (fn []
      [:div
       [cep-select @cep]])))

(defn search-results []
  (let [results (subscribe [:results])]
    (fn []
      [:div [:span @results]])))

(defn app []
  [:div "Web UI Tests"
   [message]
   [authn-panel]
   [cloud-entry-point]
   [search-results]])
