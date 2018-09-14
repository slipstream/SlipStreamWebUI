(ns sixsq.slipstream.webui.welcome.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn card [name-kw desc-kw icon target-resource]
  (let [tr (subscribe [::i18n-subs/tr])
        is-user? (subscribe [::authn-subs/is-user?])]
    [ui/Card
     [ui/CardContent {:as "h1"}
      [ui/CardHeader [ui/Header {:as "h1"}
                      [ui/Icon {:name icon}]]]
      [ui/CardDescription (@tr [desc-kw])]]
     [ui/Button {:fluid    true
                 :primary  true
                 :disabled (not @is-user?)
                 :on-click #(dispatch [::history-events/navigate target-resource])}
      (str/capitalize (@tr [name-kw]))]]))


(defmethod panel/render :welcome
  [path]
  (let [tr (subscribe [::i18n-subs/tr])
        is-admin? (subscribe [::authn-subs/is-admin?])]
    [ui/Container {:textAlign "center"
                   :fluid     true
                   :class     "webui-welcome-background"}

     [ui/Header {:as "h1"}
      (str/capitalize (@tr [:welcome]))]

     [ui/HeaderSubheader {:as "h3"}
      (@tr [:welcome-detail])]

     [ui/CardGroup {:centered true}
      [card :dashboard :welcome-dashboard-desc "dashboard" "dashboard"]
      [card :quota :welcome-quota-desc "balance scale" "quota"]
      [card :usage :welcome-usage-desc "history" "usage"]
      ;[card :appstore :welcome-appstore-desc "certificate" "appstore"]
      [card :deployment :welcome-deployment-desc "cloud" "deployment"]
      [card :application :welcome-application-desc "sitemap" "application"]
      (when @is-admin? [card :nuvlabox-ctrl :welcome-nuvlabox-desc "desktop" "nuvlabox"])
      (when @is-admin? [card :metrics :welcome-metrics-desc "bar chart" "metrics"])
      [card :cimi :welcome-cimi-desc "code" "cimi"]
      ]]))
