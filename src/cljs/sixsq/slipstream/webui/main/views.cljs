(ns sixsq.slipstream.webui.main.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.application.views]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]

    ;; all panel views must be included to define panel rendering method
    [sixsq.slipstream.webui.authn.views :as authn-views]
    [sixsq.slipstream.webui.cimi.views]
    [sixsq.slipstream.webui.dashboard.views]
    [sixsq.slipstream.webui.deployment.views]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.i18n.views :as i18n-views]
    [sixsq.slipstream.webui.legacy-application.views]
    [sixsq.slipstream.webui.legal.views]
    [sixsq.slipstream.webui.main.events :as main-events]

    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.messages.views :as messages]
    [sixsq.slipstream.webui.metrics.views]
    [sixsq.slipstream.webui.nuvlabox.views]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.profile.views]
    [sixsq.slipstream.webui.usage.views]
    [sixsq.slipstream.webui.utils.general :as utils]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.welcome.views]
    [taoensso.timbre :as log]))


(defn crumb
  [index segment]
  (let [nav-fn (fn [& _] (dispatch [::main-events/trim-breadcrumb index]))]
    ^{:key segment} [ui/BreadcrumbSection [:a {:on-click nav-fn :style {:cursor "pointer"}}
                                           (utils/truncate (str segment))]]))


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


(defn slidebar []
  (let [tr (subscribe [::i18n-subs/tr])
        show? (subscribe [::main-subs/sidebar-open?])
        nav-path (subscribe [::main-subs/nav-path])
        is-user? (subscribe [::authn-subs/is-user?])
        is-admin? (subscribe [::authn-subs/is-admin?])]
    [ui/Sidebar {:as        (ui/array-get "Menu")
                 :className "medium thin"
                 :vertical  true
                 :visible   @show?
                 :inverted  true
                 :animation "uncover"}
     (vec (concat
            [ui/Menu {:icon     "labeled"
                      :vertical true
                      :size     "large"
                      :compact  true
                      :inverted true}]
            [^{:key "logo"} [ui/MenuItem {:on-click #(dispatch [::history-events/navigate "welcome"])}
                             [ui/Image {:src "/images/cubic-logo.png" :size "tiny" :centered true}]]]
            (for [[label-kw url icon]
                  (vec
                    (concat
                      (when @is-user? [[:dashboard "dashboard" "dashboard"]
                                       [:legacy-application "legacy-application" "sitemap"]
                                       [:deployment "deployment" "cloud"]
                                       [:usage "usage" "history"]])
                      (when @is-admin?
                        [[:application "application" "sitemap"]
                         [:metrics "metrics" "bar chart"]
                         [:nuvlabox "nuvlabox" "desktop"]])
                      [[:cimi "cimi" "code"]]))
                  :when (some? label-kw)]
              [ui/MenuItem {:active  (= (first @nav-path) url)
                            :onClick (fn []
                                       (log/info "navigate event" url)
                                       (dispatch [::history-events/navigate url]))}
               [ui/Icon {:name icon}] (@tr [label-kw])])
            [[i18n-views/locale-dropdown]]))]))


(defn app []
  (fn []
    (let [show? (subscribe [::main-subs/sidebar-open?])]
      [ui/SidebarPushable {:as    (ui/array-get "Segment")
                           :basic true}
       [slidebar]
       [ui/SidebarPusher
        [ui/Container (cond-> {:id "webui-main" :fluid true}
                              @show? (assoc :className "sidebar-visible"))
         [header]
         [contents]
         [footer]]]])))
