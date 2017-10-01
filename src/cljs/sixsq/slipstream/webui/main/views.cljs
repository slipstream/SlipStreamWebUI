(ns sixsq.slipstream.webui.main.views
  (:require
    [re-com.core :refer [box h-box v-box button label modal-panel alert-box]]
    [sixsq.slipstream.webui.components.core :refer [breadcrumbs]]

    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.panel.app.views :as app-views]
    [sixsq.slipstream.webui.panel.authn.views :as authn-views]
    [sixsq.slipstream.webui.panel.cimi.views :as cimi-views]
    [sixsq.slipstream.webui.panel.deployment.views :as deployment-views]
    [sixsq.slipstream.webui.panel.empty.views :as empty-views]
    [sixsq.slipstream.webui.panel.unknown.views :as unknown-views]
    [sixsq.slipstream.webui.panel.welcome.views :as welcome-views]

    [sixsq.slipstream.webui.widget.authn.views :as authn-widget]
    [sixsq.slipstream.webui.widget.history.utils :as history]
    [sixsq.slipstream.webui.widget.history.events]
    [sixsq.slipstream.webui.widget.history.effects]

    [sixsq.slipstream.webui.widget.i18n.views :as i18n-views]
    [sixsq.slipstream.webui.widget.i18n.subs]

    [sixsq.slipstream.webui.resource :as resource]
    [sixsq.slipstream.webui.widget.breadcrumbs.utils :as u]))

(defn logo []
  [box :class "webui-logo" :width "20ex" :child ""])

(defn panel-link [label-kw url]
  (let [tr (subscribe [:webui.i18n/tr])]
    (fn [label-kw url]
      [button
       :class "btn-link webui-nav-link"
       :label (@tr [label-kw])
       :on-click #(dispatch [:evt.webui.history/navigate url])])))

(defn panel-controls []
  (let [tr (subscribe [:webui.i18n/tr])
        model (subscribe [:webui.main/nav-path])]
    (fn []
      [h-box
       :gap "2px"
       :children [[logo]
                  (doall
                    (for [[label-kw url] [[:app "application"]
                                          [:deployment "deployment"]
                                          [:cimi "cimi"]]]
                      ^{:key (name label-kw)} [panel-link label-kw url]))]])))

(defn page-header []
  [h-box
   :class "webui-header"
   :justify :between
   :children [[panel-controls]
              [h-box
               :gap "2em"
               :children [[authn-widget/authn-buttons]
                          [i18n-views/locale-selector]]]]])

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
  (let [resource-path (subscribe [:webui.main/nav-path])
        resource-query-params (subscribe [:webui.main/nav-query-params])]
    (fn []
      [v-box
       :class "webui-contents"
       :children [(resource/render @resource-path @resource-query-params)]])))

(defn header []
  (let [path (subscribe [:webui.main/nav-path])]
    (fn []
      [v-box
       :children [[page-header]
                  [breadcrumbs                              ;; FIXME: rely only on CSS for the styling
                   :model path
                   :class "webui-breadcrumbs"
                   :separator "\u2022"
                   :separator-class "webui-breadcrumbs-separator"
                   :breadcrumb-class "webui-breadcrumb-element"
                   :on-change #(dispatch [:evt.webui.history/navigate (u/breadcrumbs->url %)])]]])))

(defn footer []
  [v-box
   :children [[page-footer]]])

(defn app []
  [v-box
   :children [[alert]
              [message-modal]
              [resource-panel]]])
