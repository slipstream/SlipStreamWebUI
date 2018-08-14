(ns sixsq.slipstream.webui.main.views-sidebar
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.i18n.views :as i18n-views]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [taoensso.timbre :as log]))


(defn navigate
  "Fires a navigation event to the given URL. On small devices, this also
   forces the sidebar to close."
  [url]
  (let [device (subscribe [::main-subs/device])]
    (log/info "sidebar navigate:" url)
    (when (#{:mobile :tablet} @device)
      (dispatch [::main-events/close-sidebar]))
    (dispatch [::history-events/navigate url])))


(defn item
  [label-kw url icon]
  (let [tr (subscribe [::i18n-subs/tr])
        nav-path (subscribe [::main-subs/nav-path])]

    ^{:key (name label-kw)}
    [ui/MenuItem {:aria-label (@tr [label-kw])
                  :active     (= (first @nav-path) url)
                  :on-click   #(navigate url)}
     [ui/Icon {:name icon}]
     (@tr [label-kw])]))


(defn logo-item
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    ^{:key "welcome"}
    [ui/MenuItem {:aria-label (@tr [:welcome])
                  :on-click   #(navigate "welcome")}
     [ui/Image {:src      "/images/cubic-logo.png"
                :size     "tiny"
                :centered true}]]))


(defn menu
  "Provides the sidebar menu for selecting major components/panels of the
   application."
  []
  (let [show? (subscribe [::main-subs/sidebar-open?])
        is-user? (subscribe [::authn-subs/is-user?])
        is-admin? (subscribe [::authn-subs/is-admin?])]

    [ui/Sidebar {:as        (ui/array-get "Menu")
                 :className "medium thin"
                 :vertical  true
                 :inverted  true
                 :visible   @show?
                 :animation "uncover"}
     [:nav {:aria-label "sidebar"}
      (cond-> [ui/Menu {:icon     "labeled"
                        :size     "large"
                        :vertical true
                        :compact  true
                        :inverted true}]
              true (conj [logo-item])
              @is-user? (conj [item :deployment "deployment" "cloud"])
              @is-user? (conj [item :application "application" "sitemap"])
              @is-user? (conj [item :usage "usage" "history"])
              @is-admin? (conj [item :metrics "metrics" "bar chart"])
              @is-admin? (conj [item :nuvlabox "nuvlabox" "desktop"])
              true (conj [item :cimi "cimi" "code"])
              true (conj [i18n-views/locale-dropdown]))]]))
