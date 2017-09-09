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

(defn panel-controls []
  (let [tr (subscribe [:webui.i18n/tr])
        model (subscribe [:resource-path])]
    (fn []
      [h-box
       :gap "2px"
       :children [[accordeon-menu-ctrl]
                  (doall
                    (for [[label-kw url] [[:app "application"]
                                          [:deployment "deployment"]
                                          [:offer "offer"]
                                          [:cimi "cimi"]
                                          [:credential "credential"]]]
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
  (let [resource-path (subscribe [:resource-path])
        resource-query-params (subscribe [:resource-query-params])]
    (fn []
      [v-box
       :class "webui-contents"
       :children [(resource/render @resource-path @resource-query-params)]])))

(defn header []
  (let [path (subscribe [:resource-path])]
    (fn []
      [v-box
       :children [[page-header]

                  ;; FIXME: rely only on CSS for the styling
                  [breadcrumbs
                   :model path
                   :class "webui-breadcrumbs"
                   :separator "\u2022"
                   :separator-class "webui-breadcrumbs-separator"
                   :breadcrumb-class "webui-breadcrumb-element"
                   :on-change #(dispatch [:set-resource-path-vec %])]]])))

(defn footer []
  [v-box
   :children [[page-footer]]])

(defn accordeon-menu []
  [accordeon-menu-widget/accordeon-menu
   {:component-name "main-menu"
    :open-sections #{0 1}}
   "Applications" [{:content [:p "All"] :data-dispatch :navigation/application}
                   {:content [:p "My apps"] :data-dispatch :choice-ab}
                   {:content [:p "Shared with me"] :data-dispatch :choice-ab}
                  {:content [:p "Public"] :data-dispatch :choice-ac}]
   "Deployments" [{:content [:p "All"] :data-dispatch :navigation/deployment}
                  {:content [:p "Choice BB"] :data-dispatch :choice-bb}
                  {:content [:p "Choice BC"] :data-dispatch :choice-bc}]
   "Menu item 3" [{:content [:p "Choice CA"] :data-dispatch :choice-ca}
                  {:content [:p "Choice CB"] :data-dispatch :choice-cb}
                  {:content [:p "Choice CC"] :data-dispatch :choice-cc}]])

(defn app []
  [v-box
   :children [[alert]
              [message-modal]
              [resource-panel]]])

(defn main []
  [h-box
   :children [[accordeon-menu]
              [v-box
                :children [[header]
                           [app]
                           [footer]]]]])
