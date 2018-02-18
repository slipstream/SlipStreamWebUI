(ns sixsq.slipstream.webui.main.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    ;; all panel views must be included to define panel rendering method
    [sixsq.slipstream.webui.application.views]
    [sixsq.slipstream.webui.deployment.views]
    [sixsq.slipstream.webui.dashboard.views]
    [sixsq.slipstream.webui.metrics.views]
    [sixsq.slipstream.webui.profile.views]
    [sixsq.slipstream.webui.cimi.views]
    [sixsq.slipstream.webui.settings.views]
    [sixsq.slipstream.webui.usage.views]

    [sixsq.slipstream.webui.authn.views :as authn-views]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.i18n.views :as i18n-views]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.main.events :as main-events]

    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [reagent.core :as r]))


(defn sidebar []
  (let [tr (subscribe [::i18n-subs/tr])
        show? (subscribe [::main-subs/sidebar-open?])
        nav-path (subscribe [::main-subs/nav-path])]
    (fn []
      (when @show?
        (vec (concat
               [ui/Menu {:className  "webui-sidebar"
                         :icon       "labeled"
                         :vertical   true
                         :borderless true
                         :inverted   true}]
               [^{:key "logo"} [ui/MenuItem
                                [ui/Image {:src "/images/cubic-logo.png"}]]]
               (for [[label-kw url icon] [[:application "application" "sitemap"]
                                          [:deployment "deployment" "cloud"]
                                          [:dashboard "dashboard" "dashboard"]
                                          [:profile "profile" "user circle"]
                                          [:cimi "cimi" "code"]
                                          [:usage "usage" "history"]
                                          [:settings "settings" "settings"]
                                          [:metrics "metrics" "bar chart"]]]
                 [ui/MenuItem {:active  (= (first @nav-path) url)
                               :onClick (fn []
                                          (log/info "navigate event" url)
                                          (dispatch [::history-events/navigate url]))}
                  [ui/Icon {:name icon}] (@tr [label-kw])])))))))


(defn crumb
  [index segment]
  (let [nav-fn (fn [& _] (dispatch [::main-events/trim-breadcrumb index]))]
    ^{:key segment} [ui/BreadcrumbSection [:a {:on-click nav-fn :style {:cursor "pointer"}} (str segment)]]))


(defn breadcrumbs []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (vec (concat [ui/Breadcrumb]
                   (vec (->> @path
                             (map crumb (range))
                             (interpose [ui/BreadcrumbDivider "/"]))))))))

(defn footer []
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn []
      [:footer.webui-footer
       [:div.webui-footer-left
        [:span#release-version (str "SlipStream v")]]
       [:div.webui-footer-center
        [:span " © 2018, SixSq Sàrl"]]
       [:div.webui-footer-right
        [:span " Open source under Apache 2.0 License"]]])))


(defn contents
  []
  (let [resource-path (subscribe [::main-subs/nav-path])]
    (fn []
      [ui/Container {:class-name "webui-content", :fluid true}
       (panel/render @resource-path)])))

(defn header
  []
  (let [show? (subscribe [::main-subs/sidebar-open?])
        message (subscribe [::main-subs/message])]
    (fn []
      [:div
       [ui/Menu {:className "webui-header" :borderless true}
        [ui/MenuItem {:link    true
                      :onClick (fn [] (dispatch [::main-events/toggle-sidebar]))}
         [ui/Icon {:name (if @show? "chevron left" "bars")}]]
        [ui/MenuItem [breadcrumbs]]

        [ui/MenuMenu {:position "right"}
         [ui/MenuItem
          [i18n-views/locale-dropdown]
          [authn-views/authn-button]]]]

       (when @message
         [ui/Container
          [ui/Message {:icon            (case (:type @message)
                                          :error "exclamation"
                                          "info")
                       (:type @message) true
                       :onDismiss       #(dispatch [::main-events/clear-message])
                       :header          (:header @message)
                       :content         (:content @message)}]])
       ])))


(defn app []
  (let [session (subscribe [::authn-subs/session])]
    (fn []
      [:div.webui-wrapper
       [sidebar]
       [ui/Container {:className "webui-main" :fluid true}
        [header]
        [contents]
        [footer]]])))
