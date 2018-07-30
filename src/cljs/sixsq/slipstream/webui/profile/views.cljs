(ns sixsq.slipstream.webui.profile.views
  (:require
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.authn.subs :as authn-subs]

    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.time :as time]
    [sixsq.slipstream.webui.utils.values :as values]))


(defn tuple-to-row [[v1 v2]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (str v1)]
   [ui/TableCell {:style {:max-width     "80ex"             ;; FIXME: need to get this from parent container
                          :text-overflow "ellipsis"
                          :overflow      "hidden"}}
    v2]])


(def data-to-tuple
  (juxt (comp name first) (comp values/format-value second)))


(defn id-as-href
  [[k v :as entry]]
  (if (= "id" k)
    [k (values/as-href {:href v})]
    entry))


(defn username-as-href
  [[k v :as entry]]
  (if (= "username" k)
    [k (values/as-href {:href (str "user/" v)})]
    entry))


(defn group-table-sui
  [group-data]
  [ui/Table style/definition
   (vec (concat [ui/TableBody]
                (->> group-data
                     (map data-to-tuple)
                     (map id-as-href)
                     (map username-as-href)
                     (map tuple-to-row))))])


(def session-keys #{:id :username :roles :clientIP})


(def session-keys-order {:id 0, :username 1, :roles 2, :clientIP 3, :time-remaining 4})


(defn add-index
  [[k _ :as entry]]
  (-> k
      (session-keys-order 5)
      (cons entry)))


(defn process-session-data
  [data]
  (let [locale (subscribe [::i18n-subs/locale])]
    (let [expiry (second (first (filter #(= :expiry (first %)) data)))]
      (->> data
           (filter #(session-keys (first %)))
           (cons [:time-remaining (time/remaining expiry @locale)])
           (map add-index)
           (sort-by first)
           (map rest)))))


(defn session-info
  "Provides the user's session information."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        session @(subscribe [::authn-subs/session])
        data (process-session-data (cimi-api-utils/remove-common-attrs session))]
    [cc/collapsible-card
     (@tr [:session])
     (if session
       [group-table-sui data]
       [:p (@tr [:no-session])])]))


(defmethod panel/render :profile
  [path]
  [session-info])

