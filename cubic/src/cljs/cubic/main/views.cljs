(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.main.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    ;; all panel views must be included to define panel rendering method
    [cubic.application.views]
    [cubic.deployment.views]
    [cubic.dashboard.views]
    [cubic.profile.views]
    [cubic.cimi.views]
    [cubic.settings.views]

    [cubic.authn.views :as authn-views]
    [cubic.history.events :as history-events]
    [cubic.i18n.subs :as i18n-subs]
    [cubic.i18n.views :as i18n-views]
    [cubic.main.subs :as main-subs]
    [cubic.main.events :as main-events]

    [cubic.utils.semantic-ui :as ui]
    [taoensso.timbre :as log]
    [cubic.panel :as panel]

    [cubic.authn.subs :as authn-subs]
    [reagent.core :as r]))

(defn sidebar []
  (let [tr (subscribe [::i18n-subs/tr])
        show? (subscribe [::main-subs/sidebar-open?])
        nav-path (subscribe [::main-subs/nav-path])
        activeItem (r/atom (first @nav-path))]
    (fn []
      (when @show?
        (vec (concat
               [ui/Menu {:className  "cubic-sidebar"
                         :icon       "labeled"
                         :vertical   true
                         :borderless true
                         :inverted   true}]
               [^{:key "logo"} [ui/MenuItem
                                [ui/Image {:src "/images/cubic-logo.png"}]
                                ]]
               (for [[label-kw url icon] [[:application "application" "sitemap"]
                                          [:deployment "deployment" "cloud"]
                                          [:dashboard "dashboard" "dashboard"]
                                          [:profile "profile" "user circle"]
                                          [:cimi "cimi" "code"]
                                          [:settings "settings" "settings"]]]
                 [ui/MenuItem {:active  (= @activeItem url)
                               :onClick (fn []
                                          (log/info "navigate event" url)
                                          (reset! activeItem url)
                                          (dispatch [::history-events/navigate url]))}
                  [ui/Icon {:name icon}] (@tr [label-kw])]))))
      )))


(defn crumb
  [index segment]
  (let [nav-fn (fn [& _] (dispatch [::main-events/trim-breadcrumb index]))]
    ^{:key segment} [ui/BreadcrumbSection [:a {:on-click nav-fn} (str segment)]]))


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
      [:footer.cubic-footer
       [:div.cubic-footer-left
        [:span#release-version (str "v3.43-SNAPSHOT")]]
       [:div.cubic-footer-center
        [:span "Copyright © 2017, Charles A. Loomis, Jr."]]
       [:div.cubic-footer-right
        [:span "Apache 2.0"]]])))


(defn contents
  []
  (let [resource-path (subscribe [::main-subs/nav-path])
        resource-query-params (subscribe [::main-subs/nav-query-params])]
    (fn []
      [ui/Container {:class-name "cubic-content", :fluid true}
       (panel/render @resource-path @resource-query-params)])))

(defn header
  []
  (let [show? (subscribe [::main-subs/sidebar-open?])]
    (fn []
     [ui/Menu {:className "cubic-header" :borderless true}
      [ui/MenuItem {:link true
                    :onClick (fn [] (dispatch [::main-events/toggle-sidebar]))}
       [ui/Icon {:name (if @show? "chevron left" "bars")}]]
      [ui/MenuItem [breadcrumbs]]

      [ui/MenuMenu {:position "right"}
       [ui/MenuItem
        [i18n-views/locale-dropdown]
        [authn-views/authn-button]]]])))


(defn app []
  (let [session (subscribe [::authn-subs/session])]
    (fn []
      [:div.cubic-wrapper
       [sidebar]
       [ui/Container {:className "cubic-main" :fluid true}
        [header]
        [contents]
        [footer]]
       ]
      #_[:div

         [sidebar]
         #_[ui/Grid {
                     :style {:height "100vh" :width "100vw"} :celled true :stretched true :columns 2}
            #_[ui/GridRow
               [ui/GridColumn [ui/Container [sidebar]]]
               [ui/GridColumn [ui/Container [contents]]]]
            [ui/GridRow [ui/Container [footer]]]]]

      #_[ui/Grid {:celled true}
         [ui/GridColumn]
         [ui/GridColumn
          [ui/SegmentGroup {:attached "top"}
           [ui/Segment [header]]
           [ui/Segment [contents]]
           [ui/Segment [footer]]]]])))


#_(defn app []
    (let [session (subscribe [::authn-subs/session])]
      (fn []
        [ui/Grid {:style {:height "100vh" :maxWidth "100vw"}}
         [ui/SidebarPushable {:as (aget js/semanticUIReact "Segment") :style {:height "100vh"}}
          [sidebar]
          [ui/SidebarPusher
           [ui/SegmentGroup
            [ui/Segment [header]]
            [ui/Segment [contents]]
            [ui/Segment [footer]]]
           ;[header]
           ;[contents]
           ;[footer]
           ]]])))
