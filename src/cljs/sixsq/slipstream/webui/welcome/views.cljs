(ns sixsq.slipstream.webui.welcome.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn card [name desc icon target-resource]
  (let [is-user? (subscribe [::authn-subs/is-user?])]
    [ui/Card
     [ui/CardContent {:as "h1"}
      [ui/CardHeader [ui/Header {:as "h1"}
                      [ui/Icon {:name icon}]]]
      [ui/CardDescription desc]]
     [ui/Button {:fluid    true
                 :primary  true
                 :disabled (not @is-user?)
                 :on-click #(dispatch [::history-events/navigate target-resource])}
      (str/capitalize name)]]))


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
     [:div
      [ui/CardGroup {:centered true}
       (card (str/capitalize (@tr [:dashboard]))
             (@tr [:welcome-dashboard-desc])
             "dashboard"
             "dashboard")
       (card (str/capitalize (@tr [:quota]))
             (@tr [:welcome-quota-desc])
             "balance scale"
             "quota")
       (card (str/capitalize (@tr [:usage]))
             (@tr [:welcome-usage-desc])
             "history"
             "usage")
       ;(card (str/capitalize (@tr [:appstore]))
       ;					 	 (@tr [:welcome-appstore-desc])
       ;						 "certificate"
       ;						 "appstore")
       (card (str/capitalize (@tr [:deployment]))
             (@tr [:welcome-deployment-desc])
             "cloud"
             "deployment")
       (card (str/capitalize (@tr [:application]))
             (@tr [:welcome-application-desc])
             "sitemap"
             "application")
       (when @is-admin?
         (card (str/capitalize (@tr [:nuvlabox-ctrl]))
               (@tr [:welcome-nuvlabox-desc])
               "desktop"
               "nuvlabox"))
       (when @is-admin?
         (card (str/capitalize (@tr [:metrics]))
               (@tr [:welcome-metrics-desc])
               "bar chart"
               "metrics"))]]]))
