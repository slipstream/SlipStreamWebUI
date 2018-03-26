(ns sixsq.slipstream.webui.main.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [taoensso.timbre :as log]

    ;; all panel views must be included to define panel rendering method
    [sixsq.slipstream.webui.application.views]
    [sixsq.slipstream.webui.cimi.views]
    [sixsq.slipstream.webui.deployment.views]
    [sixsq.slipstream.webui.dashboard.views]
    [sixsq.slipstream.webui.legal.views]
    [sixsq.slipstream.webui.messages.views :as messages]
    [sixsq.slipstream.webui.metrics.views]
    [sixsq.slipstream.webui.usage.views]
    [sixsq.slipstream.webui.profile.views]
    [sixsq.slipstream.webui.welcome.views]

    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.authn.views :as authn-views]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.i18n.views :as i18n-views]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn sidebar []
  (let [tr (subscribe [::i18n-subs/tr])
        show? (subscribe [::main-subs/sidebar-open?])
        nav-path (subscribe [::main-subs/nav-path])
        is-admin? (subscribe [::authn-subs/is-admin?])
        is-user? (subscribe [::authn-subs/is-user?])]
    (fn []
      (when @show?
        (vec (concat
               [ui/Menu {:className  "webui-sidebar"
                         :icon       "labeled"
                         :vertical   true
                         :borderless true
                         :inverted   true}]
               [^{:key "logo"} [ui/MenuItem {:on-click #(dispatch [::history-events/navigate "welcome"])}
                                [ui/Image {:src "/images/cubic-logo.png"}]]]
               (for [[label-kw url icon]
                     (vec
                       (concat
                         (when @is-user? [[:dashboard "dashboard" "dashboard"]
                                          [:application "application" "sitemap"]
                                          [:deployment "deployment" "cloud"]
                                          [:usage "usage" "history"]])
                         (when @is-admin?
                           [[:metrics "metrics" "bar chart"]])
                         [[:cimi "cimi" "code"]]))
                     :when (some? label-kw)]
                 [ui/MenuItem {:active  (= (first @nav-path) url)
                               :onClick (fn []
                                          (log/info "navigate event" url)
                                          (dispatch [::history-events/navigate url]))}
                  [ui/Icon {:name icon}] (@tr [label-kw])])
               [[ui/MenuItem]
                [i18n-views/locale-dropdown]]
               ))))))


(defn crumb
  [index segment]
  (let [nav-fn (fn [& _] (dispatch [::main-events/trim-breadcrumb index]))]
    ^{:key segment} [ui/BreadcrumbSection [:a {:on-click nav-fn :style {:cursor "pointer"}} (str segment)]]))


(defn breadcrumbs []
  (let [path (subscribe [::main-subs/nav-path])]
    (fn []
      (vec (concat [ui/Breadcrumb {:size :large}]
                   (vec (->> @path
                             (map crumb (range))
                             (interpose [ui/BreadcrumbDivider {:icon "chevron right"}]))))))))

(defn footer
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [:footer.webui-footer
     [:div.webui-footer-left
      [:span "© 2018, SixSq Sàrl"]]
     [:div.webui-footer-right
      [:span
       [ui/Icon {:name "balance"}]
       [:a {:style    {:cursor "pointer"}
            :on-click #(dispatch [::history-events/navigate "legal"])}
        (@tr [:legal])]]]]))


(defn contents
  []
  (let [resource-path (subscribe [::main-subs/nav-path])]
    (fn []
      [ui/Container {:class-name "webui-content", :fluid true}
       (panel/render @resource-path)])))


(defn header
  []
  (let [show? (subscribe [::main-subs/sidebar-open?])]
    (fn []
      [:div
       [ui/Menu {:className  "webui-header"
                 :borderless true}
        [ui/MenuItem {:link     true
                      :on-click #(dispatch [::main-events/toggle-sidebar])}
         [ui/Icon {:name (if @show? "bars" "bars")}]]       ;; FIXME: Find a better close icon.  Can't look like "back" button.
        [ui/MenuItem [breadcrumbs]]

        [ui/MenuMenu {:position "right"}
         [messages/bell-menu]
         [ui/MenuItem {:fitted true}
          [authn-views/authn-menu]]]]

       [messages/message-modal]])))


(defn app []
  [:div.webui-wrapper
   [sidebar]
   [ui/Container {:className "webui-main" :fluid true}
    [header]
    [contents]
    [footer]]])
