(ns sixsq.slipstream.webui.profile.views
  (:require
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]

    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn tuple-to-row [[v1 v2]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (str v1)]
   [ui/TableCell (str v2)]])


(defn group-table-sui
  [group-data]
  (let [data (sort-by first group-data)]
    [ui/Table {:compact     true
               :definition  true
               :single-line true
               :padded      false
               :style       {:max-width "100%"}}
     (vec (concat [ui/TableBody]
                  (map tuple-to-row (map (juxt (comp name first) (comp str second)) data))))]))


(defn session-info
  "Provides the user's session information."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        session @(subscribe [::authn-subs/session])
        data (sort (cimi-api-utils/remove-common-attrs session))
        key-fn (comp name first)
        value-fn (comp str second)]
    [ui/Container
     [ui/Header {:as "h1"} (@tr [:profile])]
     (if session
       (group-table-sui data)
       [:p (@tr [:no-session])])]))


(defmethod panel/render :profile
  [path]
  [session-info])

