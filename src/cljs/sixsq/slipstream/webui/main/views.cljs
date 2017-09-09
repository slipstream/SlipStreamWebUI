(ns sixsq.slipstream.webui.main.views
  (:require
    [re-com.core :refer [box h-box v-box button label modal-panel alert-box]]
    [sixsq.slipstream.webui.components.core :refer [breadcrumbs]]

    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.main.events]

    [sixsq.slipstream.webui.panel.app.views :as app-views]
    [sixsq.slipstream.webui.panel.authn.views :as authn-views]
    [sixsq.slipstream.webui.panel.cimi.views :as cimi-views]
    [sixsq.slipstream.webui.panel.credential.views :as credential-views]
    [sixsq.slipstream.webui.panel.deployment.views :as deployment-views]
    [sixsq.slipstream.webui.panel.empty.views :as empty-views]
    [sixsq.slipstream.webui.panel.offer.views :as offer-views]
    [sixsq.slipstream.webui.panel.unknown.views :as unknown-views]
    [sixsq.slipstream.webui.panel.welcome.views :as welcome-views]

    [sixsq.slipstream.webui.widget.authn.views :as authn-widget]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.widget.history.events]
    [sixsq.slipstream.webui.widget.history.effects]

    [sixsq.slipstream.webui.widget.accordeon-menu.events]
    [sixsq.slipstream.webui.widget.accordeon-menu.subs]
    [sixsq.slipstream.webui.widget.accordeon-menu.views :as accordeon-menu-widget]

    [sixsq.slipstream.webui.widget.i18n.views :as i18n-views]
    [sixsq.slipstream.webui.widget.i18n.subs]

    [sixsq.slipstream.webui.resource :as resource]))

(defn logo []
  [box :class "webui-logo" :width "20ex" :child ""])

(defn panel-link [label-kw url]
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn [label-kw url]
      [button
       :class "btn-link webui-nav-link"
       :label (@tr [label-kw])
       :on-click #(history/navigate url)])))

(defn accordeon-menu-ctrl []
  [accordeon-menu-widget/accordeon-menu-ctrl "main-menu"])

(defn page-footer []
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn []
      [h-box
       :class "webui-footer"
       :justify :between
       :children [[label :label [:span#release-version (str "SlipStream v")]]
                  [label :label (str (@tr [:copyright]) " © 2016-2017, SixSq Sàrl")]]])))

(defn message-modal
  []
  (let [message (subscribe [:message])]
    (fn []
      (if @message
        [modal-panel
         :child @message
         :wrap-nicely? true
         :backdrop-on-click #(dispatch [:clear-message])]))))

(defn alert
  []
  (let [alert (subscribe [:webui.main/alert])]
    (fn []
      (if @alert
        (let [{:keys [alert-type heading body]
               :or   {alert-type :none, heading "Alert", body ""}} @alert]
          [alert-box
           :alert-type alert-type
           :heading heading
           :body body
           :closeable? true
           :on-close #(dispatch [:evt.webui.main/clear-alert])])))))

(defn resource-panel
  []
  (let [resource-path (subscribe [:resource-path])
        resource-query-params (subscribe [:resource-query-params])]
    (fn []
      [v-box
       :class "webui-contents"
       :children [(resource/render @resource-path @resource-query-params)]])))

(defn filter-widget []
  [:i.material-icons.md-light "filter_list"])

(defn gap []
  [:div {:style {:flex-grow 1000}}])

(defn header []
  (let [path (subscribe [:resource-path])]
    (fn []
      [h-box
       :class "webui-header"
       :justify :between
       :align :center
       :children [;; FIXME: rely only on CSS for the styling
                  [accordeon-menu-ctrl]
                  [breadcrumbs
                   :model path
                   :class "webui-breadcrumbs"
                   :separator "\u2022"
                   :separator-class "webui-breadcrumbs-separator"
                   :breadcrumb-class "webui-breadcrumb-element"
                   :on-change #(dispatch [:set-resource-path-vec %])]
                  [gap]
                  [filter-widget]
                  [authn-widget/authn-buttons]]])))

(defn footer []
  [v-box
   :children [[page-footer]]])

(defn accordeon-menu []
  [accordeon-menu-widget/accordeon-menu
   {:component-name "main-menu"
    :open-sections #{0 1}}
   "Dashboard"    [{:content [:p "Overview"] :data-dispatch :navigation/dashboard}
                   {:content [:p "Apps"] :data-dispatch :navigation/dashboard}
                   {:content [:p "Virtual machines"] :data-dispatch :navigation/dashboard}]
   "App Store"    [{:content [:p "All"] :data-dispatch :navigation/appstore}
                   {:content [:p "Public"] :data-dispatch :navigation/appstore}
                   {:content [:p "Private"] :data-dispatch :navigation/appstore}]
   "Applications" [{:content [:p "All"] :data-dispatch :navigation/application}
                   {:content [:p "My apps"] :data-dispatch :navigation/application}
                   {:content [:p "Shared with me"] :data-dispatch :navigation/application}
                   {:content [:p "Public"] :data-dispatch :navigation/application}]
   "Deployments"  [{:content [:p "All"] :data-dispatch :navigation/deployment}
                   {:content [:p "Ready"] :data-dispatch :navigation/deployment}
                   {:content [:p "Completed"] :data-dispatch :navigation/deployment}
                   {:content [:p "Failed"] :data-dispatch :navigation/deployment}]
   "Offers"       [{:content [:p "All"] :data-dispatch :navigation/offer}
                   {:content [:p "Cheaper"] :data-dispatch :navigation/offer}
                   {:content [:p "Latest"] :data-dispatch :navigation/offer}]
   "CIMI"         [{:content [:p "All"] :data-dispatch :navigation/cimi}
                   {:content [:p "Usage"] :data-dispatch :navigation/cini}
                   {:content [:p "Credentials"] :data-dispatch :navigation/cimi}]])

(defn app []
  [v-box
   :max-width "1100px"
   :width "100%"
   :align-self :center
   :children [[alert]
              [message-modal]
              [resource-panel]]])

(defn main []

  [h-box
   :size "auto"
   :style {:background-color "gray"}
   :children [[accordeon-menu]
              [v-box
               :size "auto"
               :children [[header]
                          [app]
                          [gap]
                          [footer]]]]]

  ;[h-box
  ; :children [
  ;            [v-box
  ;              :size "auto"
  ;              :children [[header]
  ;                         [app]
  ;                         [gap]
  ;                         [footer]]]]]
  )
