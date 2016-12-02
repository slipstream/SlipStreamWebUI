(ns sixsq.slipstream.webui.views
  (:require
    [re-com.core :refer [h-box v-box box gap line input-text button label throbber hyperlink-href p] :refer-macros [handler-fn]]
    [re-com.buttons :refer [button-args-desc]]
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
        (if true                                            ;;logged-in?
          [h-box
           :children [[button
                       :label user-id
                       :on-click #(js/alert (str "profile for " user-id))]
                      [button
                       :label "logout"
                       :on-click #(dispatch [:logout])]]])))))

(defn login
  []
  (let [authn (subscribe [:authn])
        username (reagent/atom "username")
        password (reagent/atom "password")]
    (fn []
      (let [{:keys [logged-in? user-id]} @authn]
        (if-not logged-in?
          [h-box
           :children [[input-text
                       :model "atom"
                       :on-change #(reset! username (-> % .-target .-value))]
                      [input-text
                       :model "atom"
                       :on-change #(reset! password (-> % .-target .-value))]
                      [button
                       :label "login"
                       :on-click #(dispatch [:login {:username @username :password @password}])]]])))))

(defn authn-panel []
  [h-box
   :children [#_[login] [logout]]])

(def common-keys
  #{:id :created :updated :acl :baseURI :resourceURI})

(defn format-link [k]
  (let [n (name k)]
    [:option {:key n :value n} n]))

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

(declare format-value)

(defn as-map [m]
  [:ul (doall (map (fn [[k v]] [:li [:strong k] " : " (format-value v)]) m))])

(defn as-vec [v]
  [:ul (doall (map (fn [k v] [:li [:strong k] " : " (format-value v)]) (range) v))])

(defn format-value [v]
  (cond
    (map? v) (as-map v)
    (vector? v) (as-vec v)
    :else (str v)))

(defn search-results []
  (let [results (subscribe [:results])]
    (fn []
      [:div (format-value @results)])))

(defn app []
  [v-box
   :children [[h-box
               :children [[button
                           :label "hello"]]
               #_[message]
               #_[authn-panel]
               #_[cloud-entry-point]
               #_[search-results]]
              [button
               :label "HELLO2"]
              ]])
